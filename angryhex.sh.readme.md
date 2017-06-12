# DEVELOPER


### SYNOPSIS

    ./dev.angryhex.sh command [argument]

### COMMANDS

    install  [all,dlv,dlvhex,box2d,agent,agent-java,agent-plugin]

    run      [client,server]

    set      [release,develop]

    archive


* `install`
  - `all` is the default argument; the parts of the installation (dlv, dlvhex, etc.) will only be installed if they are not present on the system
  - when using another argument (`dlv`, `dlvhex`, `box2d`, `agent`, `agent-java`, `agent-plugin`), a reinstallation of the component is forced

* `run`
  - `client` is the default argument

* `set`
  - `release` is set by default (because this script is implicitly used by the organisers)
  - **IMPORTANT**  you have to set `develop` once if you want to develop ;)

* `archive`
  - creates a file `angryhex.zip` which contains all files necessary for playing


---

# ORGANIZER


SYNOPSIS

    ./angryhex.sh command [argument]

COMMANDS

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
