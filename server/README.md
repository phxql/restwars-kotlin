# RESTwars server

## Commandline parameters

`--tournament [round]`: Starts the server in tournament mode. Before round `[round]` the server denies every state changing request with a `503`. After the game reaches `[round]`, a websocket notification is sent and the server starts accepting requests.

`--gameConfig [file]`: Loads the game config from `[file]`. Defaults to 'config.yml'.

`--balancingConfig [file]`: Loads the balancing config from `[file]`. Defaults to 'balancing-config.yml'.
