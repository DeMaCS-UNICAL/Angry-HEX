# Angry-HEX

Angry-HEX is an artificial player for the popular video game Angry Birds; it participated in the 2013, 2014, 2015 and 2016 [Angry Birds AI Competition](http://aibirds.org).

The agent is based on declarative knowledge bases, and features a combination of traditional imperative programming and declarative programming that allows to achieve high flexibility in strategy design and knowledge modelling. In particular, it relies on the use of *Answer Set Programming* (*ASP*) and [*HEX programs*](http://www.kr.tuwien.ac.at/research/systems/dlvhex).

*Logic programming* is used for decision tasks such as which target to hit at each shot (*tactic gameplay*) or which level to play at each turn, and which way (*strategic gameplay*).

---

## Running the agent (if you have not changed anything and you have installed everything)

 1. Open Chrome on the webpage http://chrome.angrybirds.com and make sure you are using the **SD version** of the game 
 1. `$ ./dev.angryhex.sh run server`
 1. Open another instance of the terminal
 1. `$ ./dev.angryhex.sh run client`

---

## Running the agent while you are developing

 1. `$ ./dev.angryhex.sh set develop` (you have to do this only once)
 1. `$ ./dev.angryhex.sh install agent`
   * you can also reinstall only the Java agent: `$ ./dev.angryhex.sh install agent-java`
   * you can also reinstall only the dlvhex plugin: `$ ./dev.angryhex.sh install agent-plugin`
 3. Follow the steps of the above section (**Running the agent**)

---

## Create a file which contains all files necessary for playing

 1. `$ ./dev.angryhex.sh set release`
 1. `$ ./dev.angryhex.sh archive`

---

## How to use the bash scripts

### DEVELOPERS

#### SYNOPSIS

    ./dev.angryhex.sh command [argument]

#### COMMANDS

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


### ORGANIZERS


#### SYNOPSIS

    ./angryhex.sh command [argument]

#### COMMANDS

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


---

## Detailed description of files and folders

It contains the following folders:
 - __*dlv*__
   * Contains the logic programs for the _Tactic_ and the _Strategy_ of the Angry-HEX agent
 - __*docs*__
   * Contains the website (GitHub Pages) of Angry-HEX
 - __*framework*__
   * Contains the framework provided by the organizers
 - __*src*__
   * Contains the main source code of the Angry-HEX agent

It contains the following files:
 - __*INSTALL.md*__
   * Main installation instructions
 - __*angryhex.sh*__, __*dev.angryhex.sh*__
   * Scripts to install, run, update and package the agent (the first is for users the second is for developers)
 - __*config.properties*__, __*config.properties.tournament*__
   * Properties files (the second one is specific for the Competition)
 - __*Makefile*__, __*build.xml*__
   * Files needed for build automation
 - __*.gitignore*__, __*LICENSE*__, __*README.md*__ 
   * Self-explanatory files


   
