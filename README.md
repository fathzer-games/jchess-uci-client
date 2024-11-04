# jchess-uci-client
A client wrapper for UCI engines


### Log
All communications between this library and the UCI process are logged using [SLF4J](https://www.slf4j.org/) with the 'debug' level.
All logs output by the UCI process to 'stderr' are logged with the 'warn' level.
When the UCI process is launched or exited, an event is logged with the 'info' level.
When an error occurred it is logged with the 'error' level.