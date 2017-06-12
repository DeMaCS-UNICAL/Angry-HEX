# Angry-HEX
 
Angry-HEX is an artificial player for the popular video game Angry Birds

---

## Running the agent (if you have not changed anything and you have installed everything)

 0. Open Chrome on the webpage http://chrome.angrybirds.com and make sure you are using the SD version of the game 
 0. `$ ./dev.angryhex.sh run server`
 0. Open another instance of the terminal
 0. `$ ./dev.angryhex.sh run client`

---

## Running the agent while you are developing

 0. `$ ./dev.angryhex.sh set develop` (you have to do this only once)
 0. `$ ./dev.angryhex.sh install agent`
   * you can also reinstall only the Java agent:
     `$ ./dev.angryhex.sh install agent-java`
   * you can also reinstall only the dlvhex plugin:
     `$ ./dev.angryhex.sh install agent-plugin`
 0. Follow the steps above

---

## Create a file which contains all files necessary for playing

 0. `$ ./dev.angryhex.sh set release`
 0. `$ ./dev.angryhex.sh archive`