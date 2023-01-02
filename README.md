# OpenBirdingMap

A website to view [ebird data](https://ebird.org).

![screenshot](https://github.com/sunng87/openbirdingmap/raw/main/screenshot.png)

[My own instance](https://obm.sunng.info) serves data of Beijing(CN-11) and
Jiangsu(CN-32).

## Requirement

### Runtime

- java
- mariadb

### Development

- lein
- npm
- babashka

## Build and run

### obmimport

Database migration and import tool.

- build: `lein uberjar`
- run: `java -jar obmimport.jar -d <ebd.csv>`

### obmserver

API server.

- build: `lein uberjar`
- run: `java -jar obmserver.jar`

A `config.edn` is required for running API server. See `config.edn.sample` for
reference.

### obmweb

The static website

- development: `npx shadow-cljs watch app`
- build: `npm run release`

## Deployment

We use babashka for deployment tasks. Example:

```
bb run deploy:all <ssh-host>
```
