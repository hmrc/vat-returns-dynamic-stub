# vat-returns-dynamic-stub

[![Build Status](https://travis-ci.org/hmrc/vat-returns-dynamic-stub.svg)](https://travis-ci.org/hmrc/vat-returns-dynamic-stub) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-returns-dynamic-stub/images/download.svg) ](https://bintray.com/hmrc/releases/vat-returns-dynamic-stub/_latestVersion)

## Usage

To use this stub, run the command `sbt "run 9159"`

## Data setup


### Populating the stub

To populate, send a **POST** request to `/setup/data`. The body must contain the following:

| **Key**    | **Description** | **Example** |
|------------|-----------------|-------------|
| `_id`      | The uri for the request that uniquely identifies the document in MongoDB. This should be the same as the uri of the endpoint being stubbed | `"/vat/555555555/obligations"` |
| `method`   | HTTP method used to call the stubbed endpoint | `"GET"` |
| `status`   | The status to be returned by the stubbed endpoint | `200` |
| `response` (Optional) | The body of the response to be returned by the stubbed endpoint | `{"due" : "2017-01-01"}` |

#### Example POST body

```

{
  "_id" : "/vat/returns/vrn/555555555?period-key=17AA",
  "method" : "GET",
  "status" : 200,
  "response" :{
    "periodKey": "17AA",
    "vatDueSales": 100.00,
    "vatDueAcquisitions": 100.00,
    "vatDueTotal": 200,
    "vatReclaimedCurrPeriod": 100,
    "vatDueNet": 100,
    "totalValueSalesExVAT": 500,
    "totalValuePurchasesExVAT": 1234567890123.00,
    "totalValueGoodsSuppliedExVAT": 500,
    "totalAllAcquisitionsExVAT": 500
  }
}

```

#### Response codes
| **Response code** | **Cause** |
|-------------------|-----------------|
| 200               | Successfully populated stub |   
| 400               | HTTP method specified in request body not supported |
| 500               | Error parsing body of request or an unexpected error occurred |

### Removing all data from the stub

To delete the data you added, send a **DELETE** request to `/setup/all-data`. No body is required.

#### Response codes
| **Response code** | **Cause** |
|-------------------|-----------------|
| 200               | Successfully cleared down stub |   
| 500               | Unexpected error occurred |

## Using the stub

### GET requests

Send a **GET** request to `/*url` where url=`_id` of any required data previously populated.

#### Response

The response code should be equal to that specified in the data that was populated with the same `_id`. The body of the response (if any) will be equal to that specified in the `response` field of the setup data.
If no matching `_id` exists in MongoDB, a `400` response is returned.

### POST requests

Send a **POST** request to `/*url` where url=`_id` of any required data previously populated.

#### Response

The response code should be equal to that specified in the data that was populated with the same `_id`. The body of the response (if any) will be equal to that specified in the `response` field of the setup data.
If no matching `_id` exists in MongoDB, a `404` response is returned.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
