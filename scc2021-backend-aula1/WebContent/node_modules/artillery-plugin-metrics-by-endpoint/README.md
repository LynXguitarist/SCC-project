
# Extended Purpose

Allows applications to group endpoints using a given function.
The name of the function should be given in variable metricsProcessEndpoint and should be a global function.

# Purpose

Use this plugin to get a per-endpoint breakdown of latency and response codes in your Artillery HTTP tests.

# Usage

Install the plugin globally or locally, depending on your setup

```
// global plugin installation
 npm install artillery-plugin-metrics-by-endpoint -g
 
 // local plugin installation
 npm install --save-dev artillery-plugin-metrics-by-endpoint
```
 
Enable the plugin in the config
 
```
config:
  plugins:
    metrics-by-endpoint: {}
  variables:
     metricsProcessEndpoint : "myProcessEndpoint"
```

Function *myProcessEndpoint* should be defined as a global function in the processor code.
Example function:

```
// All endpoints starting with the following prefixes will be aggregated in the same bucker for the statistics
var statsPrefix = [ ["/post/thread/","GET"],
	["/post/like/","POST"],
	["/post/unlike/","POST"],
	["/image/","GET"],
	["/post/p/","GET"],
	["/users/","GET"],
	["/community/","GET"],
 ["/media/","GET"]
	]

// Function used to compress statistics
global.myProcessEndpoint = function( str, method) {
	var i = 0;
	for( i = 0; i < statsPrefix.length; i++) {
		if( str.startsWith( statsPrefix[i][0]) && method == statsPrefix[i][1])
			return method + ":" + statsPrefix[i][0];
	}
	return method + ":" + str;
}
```



# License

MPL 2.0
