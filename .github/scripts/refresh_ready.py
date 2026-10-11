"""Osvježava Status zadataka u GitHub Projectu prema retku „Ovisi o” u tekstu issuea.

- Otvoren zadatak u stanju Backlog čije su sve ovisnosti zatvorene prelazi u Ready.
- Otvoren zadatak u stanju Ready kojem je neka ovisnost ponovno otvorena vraća se u Backlog.
- Zadaci u stanju In progress, In review i Done, epici, funkcionalnosti i stavke bez statusa se ne diraju.

Ovisnost je zadovoljena kad je issue zatvoren (svejedno je li „completed” ili „not planned”).
Ovisnost o funkcionalnosti (npr. AUTH-01) znači sve njezine podzadatke (AUTH-01-…).
„svi -TEST podzadaci … iz EPIC-01 do EPIC-09” znači sve -TEST zadatke tih epika.

Pokretanje: GH_TOKEN=<token s pravom project> python3 refresh_ready.py [--dry-run] [--assume-closed ID ...]
"""
import argparse
import json
import os
import re
import subprocess
import sys

OWNER = os.environ.get('PROJECT_OWNER', 'LD05-fk')
NUMBER = int(os.environ.get('PROJECT_NUMBER', '1'))

TASK = re.compile(r'^([A-Z]+-\d+(?:-[A-Z]+\d*)?) · \[')
FEATURE = re.compile(r'^([A-Z]+-\d+) · (?!\[)')
EPIC = re.compile(r'^(EPIC-\d+) ')
TOKEN = re.compile(r'\b(EPIC-\d+|[A-Z]+-\d+(?:-[A-Z]+\d*)?)\b')


def graphql(query, **variables):
    args = ['gh', 'api', 'graphql', '-f', f'query={query}']
    for k, v in variables.items():
        args += ['-F' if isinstance(v, int) else '-f', f'{k}={v}']
    out = subprocess.run(args, check=True, capture_output=True, text=True).stdout
    return json.loads(out)['data']


def load_project():
    q = '''query($owner: String!, $number: Int!, $after: String) {
      user(login: $owner) { projectV2(number: $number) {
        id
        field(name: "Status") { ... on ProjectV2SingleSelectField { id options { id name } } }
        items(first: 100, after: $after) {
          pageInfo { hasNextPage endCursor }
          nodes {
            id
            status: fieldValueByName(name: "Status") { ... on ProjectV2ItemFieldSingleSelectValue { name } }
            content { ... on Issue { number title state body labels(first: 20) { nodes { name } } } }
          }
        } } } }'''
    items, after, project = [], None, None
    while True:
        kw = {'owner': OWNER, 'number': NUMBER}
        if after:
            kw['after'] = after
        p = graphql(q, **kw)['user']['projectV2']
        project = project or p
        items += p['items']['nodes']
        if not p['items']['pageInfo']['hasNextPage']:
            break
        after = p['items']['pageInfo']['endCursor']
    return project, items


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true', help='samo ispiši promjene')
    ap.add_argument('--assume-closed', nargs='*', default=[], help='za probu: smatraj ove zadatke zatvorenima')
    args = ap.parse_args()

    project, items = load_project()
    status_field = project['field']
    option = {o['name']: o['id'] for o in status_field['options']}

    tasks, closed, epic_of = {}, {}, {}
    for it in items:
        c = it.get('content') or {}
        title = c.get('title') or ''
        m = TASK.match(title) or FEATURE.match(title) or EPIC.match(title)
        if not m:
            continue
        key = m.group(1)
        closed[key] = c['state'] == 'CLOSED' or key in args.assume_closed
        if TASK.match(title):
            labels = [n['name'] for n in c['labels']['nodes']]
            epic_of[key] = next((l for l in labels if l.startswith('EPIC-')), None)
            tasks[key] = dict(item=it['id'], number=c['number'], open=not closed[key],
                              status=(it.get('status') or {}).get('name'), body=c.get('body') or '')

    def expand(line):
        """Pretvori redak „Ovisi o” u skup ID-jeva zadataka."""
        deps, unknown = set(), []
        epics = set(re.findall(r'EPIC-\d+', line))
        rng = re.search(r'EPIC-(\d+) do EPIC-(\d+)', line)
        if rng:
            epics |= {f'EPIC-{n:02d}' for n in range(int(rng.group(1)), int(rng.group(2)) + 1)}
        only_test = '-TEST' in line
        for e in epics:
            deps |= {t for t, ep in epic_of.items() if ep == e and (not only_test or t.endswith('-TEST'))}
        for tok in TOKEN.findall(line):
            if tok.startswith('EPIC-'):
                continue
            if tok in tasks:
                deps.add(tok)
            elif any(t.startswith(tok + '-') for t in tasks):
                deps |= {t for t in tasks if t.startswith(tok + '-')}
            else:
                unknown.append(tok)
        return deps, unknown

    changes, warnings = [], []
    for key, t in sorted(tasks.items()):
        if not t['open'] or t['status'] not in ('Backlog', 'Ready'):
            continue
        m = re.search(r'^\*\*Ovisi o:\*\*(.*)$', t['body'], re.M)
        deps, unknown = expand(m.group(1) if m else '')
        deps.discard(key)
        if unknown:
            warnings.append(f'{key} (#{t["number"]}): nepoznati ID u „Ovisi o”: {", ".join(unknown)}')
        waiting = sorted(d for d in deps if not closed.get(d, False))
        want = 'Backlog' if waiting else 'Ready'
        if want != t['status']:
            changes.append((key, t, want, waiting))

    lines = [f'Zadataka: {len(tasks)} · promjena: {len(changes)}' + (' (proba, ništa nije upisano)' if args.dry_run else '')]
    for key, t, want, waiting in changes:
        why = f' (čeka: {", ".join(waiting)})' if waiting else ''
        lines.append(f'- {key} (#{t["number"]}): {t["status"]} → {want}{why}')
    lines += [f'- upozorenje: {w}' for w in warnings]
    print('\n'.join(lines))
    if os.environ.get('GITHUB_STEP_SUMMARY'):
        with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as f:
            f.write('\n'.join(lines) + '\n')

    if args.dry_run or not changes:
        return
    for n in range(0, len(changes), 40):
        parts = [
            f'u{k}: updateProjectV2ItemFieldValue(input: {{projectId: "{project["id"]}", itemId: "{t["item"]}", '
            f'fieldId: "{status_field["id"]}", value: {{singleSelectOptionId: "{option[want]}"}}}}) {{ clientMutationId }}'
            for k, (_, t, want, _) in enumerate(changes[n:n + 40])]
        graphql('mutation {\n' + '\n'.join(parts) + '\n}')


if __name__ == '__main__':
    sys.exit(main())
