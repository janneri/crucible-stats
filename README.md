# A stats facade for the Crucible Rest API

A small tool for getting company wide review statistics from Crucible. 

Crucible has a nice Rest API, but for observing review activity it has a few problems:
* Fetching comments for a list of reviews takes a few minutes.
* It hard to fetch company wide stats, which are based on the details of all reviews. This is a matter of privileges. As a normal Crucible user I can see my "own" reviews.

This tool has the following features:

* Caches the slow-to-fetch review details.
* Reveals the statistics of all reviews, but does not reveal the details of any review.
* Provides the following Rest API for the cached data.

## Demo UI

![preview](http://github.com/janneri/crucible-stats/raw/master/crucible-stats-ui.png)

## JSON API

* /update-cache?username=value&password=value
* /reviews?*params*
* /reviews-per-month?*params*
* /comments?*params*
* /userstats?*params*

*params:* excludedProjects, includedProjects, authors, sinceDate, minComments

The *params* are optional, but they can be used to filter reviews.

## How to run

Assuming [Leiningen](https://github.com/technomancy/leiningen) is already installed, start up the server with

```bash
lein deps
lein run
or 
lein uberjar
java -jar target/rpi-challenger-*-standalone.jar
```

Then open <http://localhost:8080/> in a web browser.

## License

Distributed under the MIT License.