# crucible_stats_facade

A stats facade for the Crucible Rest API. 

Features
* Returns statistics in json format of the reviews in Crucible. 
* Caches the fetched reviews, which is essential, because getting a list of reviews with comments takes a few minutes.
* Does not reveal the details of reviews. 


## Usage

If you use cake, substitute 'lein' with 'cake' below. Everything should work fine.

```bash
lein deps
lein run
```

## License

Distributed under the Eclipse Public License.

