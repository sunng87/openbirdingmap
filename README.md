# OpenBirdingMap

A website to view [ebird data](https://ebird.org).

![screenshot](https://github.com/sunng87/openbirdingmap/raw/main/screenshot.png)

![screenshot](https://github.com/sunng87/openbirdingmap/raw/main/screenshot2.png)

[My own instance](https://obm.sunng.info) serves data of Beijing(CN-11) and
Jiangsu(CN-32).

## Requirement

### Runtime

- java

Data is stored in a local [SQLite](https://sqlite.org) database file
(default `obm.db`, override with env `OBM_DB_FILE`), no external database
server is required.

### Development

- lein
- npm
- babashka

## Serve your own region

The pipeline is: download eBird data → build `obm.db` with `obmimport` →
run `obmserver` against it → serve `obmweb` static files.

### 1. Get the data

Download an [eBird Basic Dataset](https://ebird.org/data/download) (EBD)
package for your region (e.g. a state/province like `CN-11`). You will get a
tab-separated file like `ebd_CN-11_202001_202512_relJul-2026.txt`. Only the
main `.txt` file is needed; the `_sampling` file is not used.

### 2. Get API keys

- **eBird API key** (required): create one from your
  [eBird account](https://ebird.org/api/keygen). It is used by the importer
  to resolve species codes and localized common names.
- **xeno-canto API key** (optional): register at
  [xeno-canto.org/explore/api](https://xeno-canto.org/explore/api). It
  enables the Sounds section on species pages; the site works without it.

### 3. Build the database

```sh
cd obmimport
lein uberjar

EBIRD_API_KEY=<your-ebird-key> \
OBM_DB_FILE=$PWD/obm.db \
java -jar target/uberjar/obmimport-0.1.0-SNAPSHOT-standalone.jar \
  -d /path/to/ebd_CN-11_202001_202512_relJul-2026.txt
```

Notes:

- Repeat `-d <file>` to import multiple regions into the same `obm.db`
  (regions can also be added later by running the importer again against
  the existing file; it is idempotent per record and extends each region's
  metadata date range).
- Database migrations run automatically before the import; pass
  `--skip-migration` when re-importing into an up-to-date database.
- The import may take a few minutes for large regions.

### 4. Run the API server

```sh
cd obmserver
cp config.edn.sample config.edn   # then edit it
lein uberjar

OBM_DB_FILE=/path/to/obm.db \
java -jar target/uberjar/obmserver-0.1.0-SNAPSHOT-standalone.jar
```

`config.edn`:

```clojure
{:ebird-api-key "KEY"
 ;; optional, enables the Sounds section
 :xeno-canto-api-key "KEY"
 :http-port 8080}
```

The database path defaults to `./obm.db` (relative to the working
directory) and can be overridden with the `OBM_DB_FILE` env var.

Verify it works:

```sh
curl http://localhost:8080/api/metadata
curl http://localhost:8080/api/state/CN-11
```

### 5. Build and serve the website

```sh
cd obmweb
npm install
npm run release   # outputs static files under resources/public/
```

The frontend calls the API at `/api`, so serve `resources/public/` with any
static web server and proxy `/api` to the API server. A minimal nginx
example:

```nginx
server {
    listen 80;

    root /path/to/obmweb/resources/public;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
    }
}
```

## Development

### obmweb

Run the frontend in dev mode with hot reload; `/api` requests are proxied
to the API server on port 8080:

```sh
cd obmweb
npm install
npx shadow-cljs watch app
# browse http://localhost:8280/
```

### obmserver / obmimport

Both are plain Clojure projects: `lein run` to run, `lein test` for tests.

## Deployment

We use babashka for deployment tasks. Example:

```
bb run deploy:all <ssh-host>
```
