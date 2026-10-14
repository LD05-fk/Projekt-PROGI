#!/usr/bin/env bash
# Puni lokalnu bazu testnim podacima: pokreće backend s profilom "dev" i Liquibase kontekstima
# "dev,dev-seed", pa se primijene i changesetovi s context: dev-seed (testni korisnici, demo djela)
# koje obično preskačemo. Na produkciji ovo ne postoji, tamo je SPRING_LIQUIBASE_CONTEXTS=prod.
#
# Baza mora biti pokrenuta: docker compose up -d
set -euo pipefail

cd "$(dirname "$0")/../backend"

export SPRING_PROFILES_ACTIVE=dev
export SPRING_LIQUIBASE_CONTEXTS=dev,dev-seed

echo "Pokrećem backend (profil dev, konteksti dev,dev-seed)."
echo "Kad se aplikacija pokrene i Liquibase završi, zaustavi je s Ctrl+C."

exec ./mvnw spring-boot:run
