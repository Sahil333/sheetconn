# SheetConn

Java/Spring backend for importing PostgreSQL query results into Google Sheets.

## Local configuration

Copy `.env.example` to `.env` and fill in `POSTGRES_PASSWORD`,
`GOOGLE_OAUTH_CLIENT_ID`, and `GOOGLE_OAUTH_CLIENT_SECRET` locally. The Google
credentials must belong to your own OAuth client. If sign-in uses additional
Google clients, set `GOOGLE_OAUTH_AUDIENCES` to their comma-separated client IDs.

Load the variables in your shell before starting Spring; Spring does not load
`.env` automatically. Docker Compose reads the same `.env` file for PostgreSQL.

```sh
cp .env.example .env
# Edit .env locally before continuing.
set -a
. ./.env
set +a
docker compose up -d
./gradlew bootRun
```

The default database address is `localhost:5432`, database `workbookDb`, and user
`test_user`. Credentials and private-key files are ignored by Git.

## Credential checks

Run `python3 scripts/check-secrets.py` before committing. The same check runs on
pushes and pull requests. It rejects recognized credential formats and literal
OAuth client secrets in tracked configuration files, without printing values.
It is a guard against common leaks, not a complete secret scanner; it does not
scan Git history.

Credentials previously committed to this repository must be considered exposed.
Rotate the Google OAuth client secret in Google Auth Platform and disable the old
secret. Replace the removed private key anywhere it was used. Removing these
values from the current files does not remove them from earlier commits.
