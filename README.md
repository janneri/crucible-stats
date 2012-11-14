# A stats facade for the Crucible Rest API

A small tool for getting company wide review statistics from Crucible. 

Crucible has a nice Rest API, but for observing review activity it has a few problems:
* Fetching comments for a list of reviews takes a few minutes.
* It hard to fetch company wide stats, which are based on the details of all reviews. This is a matter of privileges. As a Crucible User I can see only a few reviews, but I should be able to see the statistics derived from all the reviews.

This tool has the following features:

* Caches the slow-to-fetch review details.
* Reveals the statistics of all reviews, but does not reveal the details of any review.
* Provides the following Rest API for the cached data.

## JSON API

* /update-cache?username=value&password=value
* /reviews?*params*
* /reviews-per-month?*params*
* /comments?*params*
* /userstats?*params*

*params:* excludedProjects, includedProjects, authors, sinceDate, minComments

The *params* are optional, but they can be used to filter reviews.


## How to Run

Clone this code, and run

```bash
lein deps
lein run
```
Call the /update-cache for instance with a nighly cron job. 

## License

Distributed under the Eclipse Public License.

