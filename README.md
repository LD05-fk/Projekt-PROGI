# ArtConnect (Projekt PROGI)

[![CI](https://github.com/LD05-fk/Projekt-PROGI/actions/workflows/ci.yml/badge.svg)](https://github.com/LD05-fk/Projekt-PROGI/actions/workflows/ci.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=LD05-fk_Projekt-PROGI&metric=coverage)](https://sonarcloud.io/summary/new_code?id=LD05-fk_Projekt-PROGI)

Projekt iz kolegija Programsko inženjerstvo (FER, 2026./2027.): web aplikacija u kojoj mladi umjetnici vode portfolio i prijavljuju se na prilike (natječaje, izložbe, radionice) koje objavljuju organizatori.

- **Zadaci:** [Issues](https://github.com/LD05-fk/Projekt-PROGI/issues), faze kao [Milestones](https://github.com/LD05-fk/Projekt-PROGI/milestones)
- **Ploča:** [ArtConnect - razvoj](https://github.com/users/LD05-fk/projects/1); pogled „Za preuzimanje” prikazuje slobodne zadatke redom rada
- **Kvaliteta koda:** [SonarCloud](https://sonarcloud.io/project/overview?id=LD05-fk_Projekt-PROGI), pregled u pozadini (jednom tjedno), ne utječe na pull requestove

## Server

**<https://artconnect-fer.duckdns.org>**

Poslužitelj (AWS EC2) nije stalno upaljen, da se štede AWS krediti. Pali ga i gasi bilo koji član tima, bez AWS računa:

1. [Actions → Server](https://github.com/LD05-fk/Projekt-PROGI/actions/workflows/server.yml) → **Run workflow**
2. odaberite `start`, `stop` ili `status` → **Run workflow**
3. nakon `start` stranica radi za 1–2 minute; stanje i adresa su u sažetku pokretanja

Kad vam više ne treba, ugasite ga (`stop`). Svaku noć u 1:00 UTC poslužitelj se ionako sam ugasi; u razdobljima ocjenjivanja to je isključeno (varijabla repozitorija `ALWAYS_ON=true`). Detalji postavljanja: [poglavlje 6](docs/6-Postavljanje-instalacija-i-konfiguracija.md#63-javno-postavljanje-i-administracija).

## Kako pokrenuti

Upute za lokalno pokretanje i deploy na EC2: [HOW_TO_RUN.md](HOW_TO_RUN.md). Rad s bazom i Liquibaseom: [db/README.md](db/README.md).

## Kako čitati ID zadatka

`EPIC-01` je epik (područje sustava). `AUTH-02` je funkcionalnost, a `AUTH-02-BE`, `AUTH-02-DES`, `AUTH-02-FE`, `AUTH-02-TEST` su njezini podzadaci po ulogama. Zadatak samo jedne uloge nema kod na kraju (npr. `FND-01`). Kodovi uloga: `DB` Database, `BE` Backend, `DES` UI Designer, `FE` Frontend, `TEST` Testing, `OPS` DevOps, `DOC` Documentation.

## Automatizacija

Workflow [Osvježi Ready](.github/workflows/refresh-ready.yml) nakon zatvaranja ili ponovnog otvaranja issuea prebacuje zadatke iz Backlog u Ready kad su zatvoreni svi zadaci iz njihovog retka „Ovisi o” (i obrnuto). Treba tajnu repozitorija `PROJECT_TOKEN`: classic token s pravom `project`.

Workflow [Server](.github/workflows/server.yml) pali i gasi produkcijski poslužitelj (vidi [Server](#server)). Treba tajne `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` (IAM korisnik koji smije samo paliti i gasiti ovu instancu) i `EC2_INSTANCE_ID`.

## Doprinos

`master` je stabilna grana i ocjenjuje se njezino stanje. Promjene ulaze isključivo kroz pull requestove.

- **Grane:** `prefiks/KOD-ZADATKA`, uz broj podzadatka ako ga ima, npr. `feature/AUTH-01-BE1`. Prefiks je obično `feature`, a prema vrsti promjene može biti i `fix`, `docs`, `chore`…
- **Commit poruke:** `tip: poruka` na hrvatskom, npr. `fix: ispravi preusmjeravanje nakon prijave`. Tipovi: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`.
- **Pull request:** jedan zadatak po PR-u; u opisu `Closes #broj` (broj issuea).
- **Uvjeti za spajanje:** zelen CI (provjere `Backend` i `Frontend`) i 1 odobrenje od osobe koja nije autor. Recenzent stvarno pročita i isproba promjenu; autor odredi konkretnog recenzenta.
- **Testovi:** osnovni testovi za poslovnu logiku i glavne tokove; ne lovi se postotak pokrivenosti. [SonarCloud](https://sonarcloud.io/project/overview?id=LD05-fk_Projekt-PROGI) analizira `master` u pozadini (jednom tjedno ili ručno: Actions → SonarCloud → Run workflow) i ne pojavljuje se na pull requestovima.
- **Tajne** (`.env`, ključevi, tokeni) nikad ne idu u repozitorij.

<a id="ai-usage"></a>
## Uporaba umjetne inteligencije

Projekt slijedi FER-ova [pravila o primjerenoj uporabi umjetne inteligencije](https://www.fer.unizg.hr/_download/repository/Policy%20on%20the%20appropriate%20use%20of%20artificial%20intelligence%20at%20the%20faculty%20of%20electrical%20engineering%20and%20computing%5B1%5D.pdf).

**Izjava:** Tim nema prethodnog iskustva u vođenju projekta ovog opsega, pa je voditelj tima koristio Claude Code (Anthropic) kao pomoć u organizaciji i infrastrukturi: za razradu zahtjeva iz projektnog zadatka u epike, funkcionalnosti i zadatke (GitHub Issues) te za pripremu CI/CD pipelinea (GitHub Actions, Docker, isporuka na AWS EC2) i pripadne dokumentacije. Opseg, prioritete i odluke donio je tim; generirani zadaci, konfiguracija i tekst pregledani su i ispravljeni prije objave, a infrastruktura je provjerena u radu (CI, isporuka, javna adresa). U alate nisu uneseni pristupni podaci ni osobni podaci. Detaljan zapis: [Home – Izjava o uporabi umjetne inteligencije](docs/Home.md#izjava-o-uporabi-umjetne-inteligencije).

## Predložak

Struktura dokumentacije i workflow za PlantUML temelje se na [predlošku kolegija Programsko inženjerstvo](https://github.com/VladoSruk/Programsko-inzenjerstvo) (autor Vlado Sruk, CC BY-NC-SA 4.0).
