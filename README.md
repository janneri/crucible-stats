# A stats facade for the Crucible Rest API. 

A small tool for getting company wide review statistics from Crucible. 

Crucible has a nice Rest API, but for statistical purpose it has problems:
* Fetching comments for a list of reviews takes a few minutes.
* It hard to fetch company wide stats, which are based on the details of all reviews.
** Review details are and must be hidden, but the statistics of them are no secret

This tool has the following features:

* Caches the slow-to-fetch review details.
* Does not reveal the details of reviews.
* Provides a Rest API for the cached data.

## Json API

* /update-cache?username=<value>&password=<value>
* /reviews?<params>
* /reviews-per-month?<params>
* /comments?<params>
* /userstats?<params>

The following optional params can be used to filter reviews:
* excludedProjects includedProjects authors sinceDate minComments

## How to Run

Clone this code, and run

```bash
lein deps
lein run
```
Call the /update-cache for instance with a nighly cron job. 

## License

Distributed under the Eclipse Public License.

