### SYNOPSIS

    ./angryhex.sh command [argument]

### COMMANDS

    install  [all,dlv,dlvhex,box2d,agent]

    run

    update


* `install`
  - `all` is the default argument
  - if no agent is present, a ZIP file will be downloaded (the result of `./dev.angryhex.sh archive`)
  - the invocation will be redirected to `dev.angryhex.sh` (the script is part of the ZIP file)

* `run`
  - the invocation will be redirected to `dev.angryhex.sh`

* `update`
  - the (possibly) new agent will be downloaded and `./dev.angryhex.sh install` will be invoked
