# cinema-hometask

## Summary/Thoughts

That was fun little task. I am coming from Java background (with some limited Kotlin experience), mostly doing services in Spring/Dropwizard. For this assignment I went with Kotlin + Ktor setup, first time. Generated example project and tried to put together something reasonable. If I am not sticking to some estabilished framework convenion, it's because of that...

## How to build

It is typical gradle app developed on `Java 21`. To build it, you just need to run :

```
./gradlew clean build
```

It will generate fat jar under `build/libs`

## How to run

* App is running on port 8080
* OpenAPI console under [localhost:8080/openapi](http://localhost:8080/openapi)
* *Internal endpoints are secured behind Http Basic auth user/pass: admin/admin*
* DateTimes everywhere (db/requests etc) have format `2024-12-01 12:00`

### Providing api key

Although app will work if no OMDB api key is provided, you will get empty responses while fetching movie details. Key is read form env variables, you can export temporarily like this :

```
export OMDB_KEY=//key provided
```

If all else fails, you can just paste it into `OmdbIntegration.kt` file where it is read.

### From IntelliJ

Import project, then run `main` method from `Application.kt` file. Remember that OMDB_KEY should be exported in same session.

### Building a fat jar

From project directory :

```
./gradlew clean build

java -jar build/libs/cinema-manager.jar
```

## Usage

All model classes have IDs generated as random UUID. This makes testing pain. First fetch all movies list, then fetch id of some movie, and then do whatever else... Why UUID? Using OMDBID makes us vendor dependent. As for screenings, it is tempting to have screening time as primary key, until our cinema finds some basement and decides to put tv there and call it "Room nr 2". Money is simple int, I assume single cinema does not need currency. Or timezones for that matter, that's why timestamps are 2024-12-01 12:00`. 

API can be used in few different ways, depending how frontend wants to implement :
* There is aggregate `/movies` endpoint that will give you every movie with screenings (but no details currently, as we could go over OMDB rate limits - problem solvable with any simple cache)
* there is `/movies/{movieId}` endpoint that will give you screenings and details in single request
* there are separate `/movies/{movieId}/details` `/movies/{movieId}/rating` `/movies/{movieId}/screenings` endpoins if frontend would want some more granular approach

## What this project lacks

* Proper dependency injection - as Ktor does not seem to provide it out of the box, unless experimental features or some external projects (kodein). I did not have time to sink in that rabbit hole.
* Better configuration - properties are extracted in messy way currently.
* Proper integration testing - current version is more like smoke tests, did not have time to properly setup requests/responses etc.
* Cache - first low hanging fruit after initial development, is having map based cache over OMDB responses, that would solve problem of rate limiting for a long time.
