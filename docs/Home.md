# ArtConnect — Projektna dokumentacija

**Kolegij:** [Programsko inženjerstvo](https://www.fer.unizg.hr/predmet/proinz), Fakultet elektrotehnike i računarstva Sveučilišta u Zagrebu
**Akademska godina:** 2026./2027.
**Nastavnik:** Vlado Sruk
**Asistentica:** Eda Jovičić

## Pregled projekta

ArtConnect je web aplikacija u kojoj mladi umjetnici vode portfolio i prijavljuju se na prilike (natječaje, izložbe, radionice) koje objavljuju organizatori.

## Članovi tima i uloge

| Ime i prezime | GitHub korisničko ime | Glavne uloge 		| Sporedne uloge		|
| Luka Dakić    | LD05-fk               | Team Lead		| Documentation, DevOps		|

## Organizacija i proces

Zadaci se prate kao [GitHub Issues](https://github.com/LD05-fk/Projekt-PROGI/issues), faze kao Milestones, a ploča je projekt „ArtConnect - razvoj".

Pravila grananja, commit poruka i pull requestova opisana su u [README-u, odjeljak „Doprinos"](../README.md#doprinos).

**Zadaci i faze.** Svaki zadatak je jedan Issue s oznakama uloge, složenosti i epika, a pripada Milestoneu svoje faze. Na ploči „ArtConnect - razvoj” prolazi stupce Backlog → Ready → In progress → In review → Done. Zadatak sam prelazi u Ready kad su zatvoreni svi zadaci o kojima ovisi (GitHub Action „Osvježi Ready”), a u Done kad se zatvori, odnosno kad se spoji pull request s `Closes #broj`. Novi zadatak otvara se predloškom „Zadatak”.

**Kraj faze.** Nedovršeni zadaci se ne brišu i ne zatvaraju, nego prelaze u sljedeću fazu: na kartici **Issues** filtrirajte `is:open milestone:"<faza koja završava>"`, označite zadatke i u izborniku **Milestone** odaberite sljedeću fazu (ili pojedinačno, u issueu desno pod *Milestone*). Tako se na ploči i u izvještaju vidi što je preneseno.
