https://openexchangerates.org/api/latest.json?app_id=YOUR_APP_ID

https://openexchangerates.org/api/latest.json?app_id=f5d35ca994d94d3691fe4cd1bcea81b1
f5d35ca994d94d3691fe4cd1bcea81b1


```shell
curl --request GET \
     --url 'https://openexchangerates.org/api/latest.json?app_id=Required&base=Optional&symbols=Optional&prettyprint=false&show_alternative=false' \
     --header 'accept: application/json'
```


```json
{
    "disclaimer": "Usage subject to terms: https://openexchangerates.org/terms",
    "license": "https://openexchangerates.org/license",
    "timestamp": 1746684010,
    "base": "USD",
    "rates": {
        "AED": 3.67299,
        "AFN": 71.5
    }
}
```