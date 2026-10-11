# Kako pokrenuti ArtConnect

## O aplikaciji

Tri dijela, u zasebnim direktorijima:

| Dio | Tehnologija | Direktorij | Lokalni port |
| Frontend | React + Vite + TypeScript | `frontend/` | 5173 |
| Backend | Java 25, Spring Boot 4 | `backend/` | 8080 |
| Baza | PostgreSQL 17 u Dockeru | `docker-compose.yml` | 5432 |

Lokalno se **samo baza vrti u Dockeru**; backend i frontend pokrećete izravno (brži restart, debugger). Na EC2 sve radi u Dockeru (`docker-compose.prod.yml`).

U razvoju Vite proxya `/api`, `/oauth2`, `/login/oauth2` i `/actuator` na backend (`frontend/vite.config.ts`), pa u pregledniku sve izgleda kao jedna domena.

## Preduvjeti

- Docker + Docker Compose (`docker compose version`)
- JDK 25 (`JAVA_HOME` postavljen)
- Node.js (LTS) i npm
- Slobodni portovi 5432, 8080, 5173

## Lokalno pokretanje

Redoslijed: baza, backend, frontend (svaki u svom terminalu).

### 1. Baza

```bash
# iz korijena repozitorija
docker compose up -d
docker compose ps          # db mora biti "healthy"
```

Zadane vrijednosti (`artconnect` / `artconnect` / `artconnect`) odgovaraju `backend/src/main/resources/application.yml`, pa `.env` nije potreban. Ako želite druge, `cp .env.example .env` i izmijenite, a backend pokrenite s istim `DB_*` varijablama.

Spajanje na bazu:

```bash
docker compose exec db psql -U artconnect artconnect
```

```sql
\dt                -- popis tablica
\q                 -- izlaz
```

Zaustavljanje i brisanje podataka:

```bash
docker compose down        # zaustavi, podaci ostaju
docker compose down -v     # zaustavi i OBRIŠI sve podatke u bazi
```

Shemu baze **ne uređujete ručno**: stvara je Liquibase pri pokretanju backenda. Kako dodati promjenu: [db/README.md](db/README.md).

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

Provjera: <http://localhost:8080/actuator/health> vraća `{"status":"UP"}`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Aplikacija: <http://localhost:5173/>

Korisne naredbe: `npm run lint`, `npm run typecheck`, `npm test`, `npm run format`.

## Deploy na EC2

Sve (baza, backend, Caddy) vrti se kroz `docker-compose.prod.yml`. Caddy servira izgrađeni frontend i proxya API na backend, pa je cijela aplikacija na jednoj adresi.

> **Status:** deploy je automatski: nakon spajanja u `master` i zelenog CI-ja workflow **CD** objavi imagee u GitHub Container Registry i isporuči ih na poslužitelj (`docs/6-…` §6.3). Ručni postupak ispod je rezerva.

### Jednokratna priprema

1. EC2 instanca (Ubuntu), sigurnosna grupa: dolazno **22** (SSH, najbolje samo vaš IP), **80** i **443**. Portove 5432 i 8080 **ne otvarati**.
2. Instalirati Docker i Node.js:
   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER   # odjava i ponovna prijava
   # Node.js (LTS) za izgradnju frontenda, npr. preko nvm ili NodeSource
   ```
3. Klonirati repozitorij i napraviti `.env`:
   ```bash
   git clone https://github.com/LD05-fk/Projekt-PROGI.git
   cd Projekt-PROGI
   cp .env.example .env
   nano .env   # postavi JAKU DB_PASSWORD; SITE_ADDRESS=:80 ili domena
   ```

### Ručni deploy (rezervni postupak)

```bash
cd Projekt-PROGI
git pull

# gradi imagee na serveru (backend i Caddy s frontendom) umjesto povlačenja iz registryja
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
```

Provjera: `http://<IP-ili-domena>/actuator/health` vraća `UP`.

Korisno:

```bash
docker compose -f docker-compose.prod.yml logs -f backend   # logovi
docker compose -f docker-compose.prod.yml down              # zaustavi (podaci ostaju)
```

### Napomene

- Domena: postupak je u poglavlju „Domena i HTTPS (DuckDNS)” niže. Bez domene `SITE_ADDRESS` ostaje `:80` (HTTP na IP adresi).
- OAuth (prijava): u postavkama OAuth aplikacije dodajte redirect URI koji odgovara adresi na EC2. Tajne (client id/secret) idu u `.env` na serveru, **nikad u repozitorij**.
- Baza je dostupna samo unutar Docker mreže. Za pristup: `docker compose -f docker-compose.prod.yml exec db psql -U <DB_USER> <DB_NAME>`.
- Seed podaci s kontekstom `dev` na produkciji se ne primjenjuju (`SPRING_LIQUIBASE_CONTEXTS=prod`).

## Domena i HTTPS (DuckDNS)

> **Status:** još nije postavljeno. Ovo su koraci za trenutak kad EC2 postoji. Poslužitelj se pali i gasi po potrebi, pa **ne koristimo Elastic IP**: javna IP se mijenja pri svakom Stop/Start, a skripta ispod je pri pokretanju javlja DuckDNS-u.

1. **Registracija:** <https://www.duckdns.org>, prijava (GitHub/Google, po mogućnosti timski račun). U polje *sub domain* upišite ime i kliknite **add domain**; ako je zauzeto, DuckDNS to javi. Ime `artconnect` je zauzeto, pa je registrirano **`artconnect-fer`**, tj. domena `artconnect-fer.duckdns.org`.
2. **Token** se vidi pri vrhu stranice. To je **tajna**: ne ide u repozitorij ni u chat. Čuva se u dijeljenom mjestu za tajne (vidi TODO, dio 6) i na serveru u datoteci koju čita samo root.
3. **Na EC2 (jednom):** datoteka s podacima i systemd servis koji se izvrši pri svakom pokretanju:
   ```bash
   sudo tee /etc/duckdns.env >/dev/null <<'EOF'
   DUCKDNS_DOMAIN=artconnect-fer
   DUCKDNS_TOKEN=<token>
   EOF
   sudo chmod 600 /etc/duckdns.env

   sudo tee /etc/systemd/system/duckdns.service >/dev/null <<'EOF'
   [Unit]
   Description=Update DuckDNS
   After=network-online.target
   Wants=network-online.target

   [Service]
   Type=oneshot
   EnvironmentFile=/etc/duckdns.env
   ExecStart=/usr/bin/curl -fsS "https://www.duckdns.org/update?domains=${DUCKDNS_DOMAIN}&token=${DUCKDNS_TOKEN}&ip="

   [Install]
   WantedBy=multi-user.target
   EOF

   sudo systemctl daemon-reload
   sudo systemctl enable --now duckdns.service
   journalctl -u duckdns.service -n 5   # treba pisati OK
   ```
   Prazan `ip=` znači da DuckDNS sam uzme adresu s koje dolazi zahtjev, tj. javnu IP instance.
4. **Provjera:** `getent hosts artconnect-fer.duckdns.org` mora dati javnu IP adresu instance (vidi se u EC2 konzoli).
5. **HTTPS:** u `.env` na serveru postavite `SITE_ADDRESS=artconnect-fer.duckdns.org` i pokrenite `docker compose -f docker-compose.prod.yml up -d`. Caddy sam nabavi certifikat od Let's Encrypta; treba mu otvoren **port 80 i 443** i DNS koji već pokazuje na instancu. Certifikat je u volumenu `caddy-data`, pa **nemojte raditi `down -v`** (briše i certifikat i bazu).
6. Nakon svakog Stop/Start instance servis opet javi novu IP; ako stranica nekoliko minuta ne radi, provjerite `journalctl -u duckdns.service`.

## Google OAuth

> **Status:** Google Cloud projekt „ArtConnect” i OAuth klijent postoje (FND-13); vlasnik je voditelj tima. Backend ih koristi kad dobije Google prijavu (AUTH-01-BE1).

**Treba li vam Google prijava lokalno?**
- **Radite na samoj prijavi** (AUTH-01-BE1, AUTH-01-FE, ručna provjera prijave): **da**. Trebate `GOOGLE_CLIENT_ID` i `GOOGLE_CLIENT_SECRET` u svom `.env` i svoj Google e-mail na popisu *Test users* (postupak niže).
- **Sav ostali rad** (portfolio, prilike, prijave, rezultati, administracija…): **ne**. Trebate samo biti prijavljeni kao neka uloga, a za to lokalno (profil `dev`) postoji zamjenska prijava `POST /api/v1/dev/login-as` s testnim korisnicima (`umjetnik@artconnect.test`, `organizator@artconnect.test`, `admin@artconnect.test`, `novi@artconnect.test`); dolazi uz AUTH zadatke. Tako se prijavljuju i Selenium testovi. Na EC2 (profil `prod`) zamjenska prijava ne postoji, ondje se prijavljuje samo Googleom.

**Kako se dobiva pristup:**
- **Google e-mail u Test users:** aplikacija je u načinu *Testing*, pa se Googleom može prijaviti samo tko je na popisu. Javite voditelju tima Google račun kojim se želite prijavljivati (lokalno ili na javnoj adresi).
- **`GOOGLE_CLIENT_ID` i `GOOGLE_CLIENT_SECRET`:** daje voditelj tima na zahtjev, **jednokratnim linkom** koji se briše nakon otvaranja (npr. Bitwarden Send, onetimesecret.com), nikad običnom porukom na Discordu.
- Vrijednosti upišite u svoj `.env` (`cp .env.example .env`, pa maknite `#` ispred Google redaka). `.env` je u `.gitignore` i **nikad ne ide u repozitorij**.

**Postavke klijenta** (Google Cloud konzola → Google Auth Platform):
- Audience: *External*, status *Testing*; scopes `openid`, `email`, `profile`.
- Client: *Web application*, Authorized JavaScript origins `http://localhost:5173` i `https://artconnect-fer.duckdns.org`; Authorized redirect URIs:
  - lokalno: `http://localhost:5173/login/oauth2/code/google` (zahtjev ide kroz Vite proxy)
  - EC2: `https://artconnect-fer.duckdns.org/login/oauth2/code/google`
- Client secret se u konzoli vidi samo pri izradi; ako se izgubi, napravi se novi i proslijedi onima koji ga koriste.

Spring Boot `.env` ne čita sam (čita ga samo Docker Compose). Kad backend dobije Google prijavu, varijable mu se predaju pri pokretanju, npr. `set -a; source ../.env; set +a` prije `./mvnw spring-boot:run` ili kroz IntelliJ run konfiguraciju. Na EC2 su u `.env` poslužitelja.

## Mogući problemi

| Problem | Rješenje |
|---|---|
| `port is already allocated` (5432) | Zaustavite lokalni Postgres (`sudo systemctl stop postgresql`) ili promijenite lijevi port u `docker-compose.yml`. |
| Backend: `Connection refused` / ne vidi bazu | `docker compose ps`: je li `db` pokrenut i `healthy`? |
| Backend: `password authentication failed` | Baza je napravljena s drugim podacima. `docker compose down -v` pa ponovno `up -d` (briše podatke). |
| `./mvnw: Permission denied` | `chmod +x backend/mvnw` |
| Pogrešna verzija Jave | `java -version` mora biti 25. |
| Docker daemon ne radi | Pokrenite Docker (`sudo systemctl start docker` ili Docker Desktop). |
| Nakon deploya stara stranica | `npm run build` nije pokrenut ili je `frontend/dist` prazan. |
