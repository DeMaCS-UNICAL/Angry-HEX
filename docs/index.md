# Angry-HEX

_Angry-HEX_ is a joint development of groups at Università della Calabria ([UNICAL](http://www.mat.unical.it)), Technische Universität Wien ([TUWIEN](http://www.kr.tuwien.ac.at)), Marmara University ([MARMARA](http://www.knowlp.com)), and Max Planck Institut für Informatik ([MPI](http://www.mpi-inf.mpg.de/departments/databases-and-information-systems/)).

A distinctive characteristic of our agent is that it uses a declarative, logic programming based module for reasoning about the target to shoot at next.
It is realized as a so-called _HEX-program_, i.e., by means of Answer Set Programming (ASP) with external sources and other extensions.

## The Angry-HEX agent
Our agent, called _AngryHEX_, builds on the Base Framework provided by the organizers and extends it with declarative means for decision making models by means of an Answer Set Programming (ASP).
Declarative logic programming controls two different layers for _AngryHEX_: the **Tactics** layer, which plans shots, and decides how to complete a level; and the **Strategy** layer, which decides the order of levels to play and repeated attempts to solve the same level.

Tactics is declaratively realized by _HEX-programs_, i.e., an extension of ASP to incorporate external sources of computation via so-called _external atoms_.
It is implemented using the _DLVHEX_ solver and computes optimal shots based on information about the current scene and on domain knowledge modeled within the _HEX-program_.
Its _input_ comprises scene information encoded as a set of logic program facts (position, size and orientation of pigs, ice, wood and stone blocks, slingshot, etc.); its _output_ are _answer sets_ that contain a dedicated atom describing the target to hit, and further information about the required shot.
Physics simulation results and other information are accessed via external atoms.

The **Strategy** layer decides, at the end of each level, which level to play next.
This layer is also realized declaratively as an (ordinary) ASP program encoding our strategy on three priority levels: (1) each available level is played once; (2) levels where the agent score differs most from the current best score are selected; (3) levels where _AngryHEX_ achieved a score higher than the current best scores and that have the minimum difference from the best score, are selected.
For each level, the **Strategy** layer keeps tracks of previously achieved scores and previously selected initial target objects.

## Core Team
 - Francesco Calimeri [2013-2017]
 - Michael Fink [2013-2015]
 - Valeria Fionda [2016-2017]
 - Stefano Germano [2013-2017]
 - Andreas Humenberger [2014-2015]
 - Giovambattista Ianni [2013-2017]
 - Aldo Marzullo [2017]
 - Christoph Redl [2013-2017]
 - Zeynep G. Saribatur [2016-2017]
 - Peter Schüller [2016-2017]
 - Daria Stepanova [2014-2017]
 - Andrea Tucci [2014-2015]
 - Anton Wimmer [2013]

### Contacts
angryhex@mat.unical.it

## License
  [GNU Affero General Public License](../LICENSE)

## Publications
