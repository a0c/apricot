apricot
=======

Apricot - High-Level Decision Diagrams based Hardware Verification Framework

ApricotCAD allows you to convert VHDL design representation to HLDDs as well
as assertions described in IEEE-1850 PSL to THLDD. Further you can simulate
the design with a predetermined or random stimuli, analyze the obtained
structural coverage and check assertions.

More on the framework:  
http://apricot.ati.ttu.ee/


Source code of ApricotCAD is handled as an IntelliJ IDEA project.
Developers should run

```sh
git update-index --assume-unchanged .idea/workspace.xml
```

to make Git ignore constant changes made to workspace.xml by IDEA.
To add important changes to it, temporarily run

```sh
git update-index --no-assume-unchanged .idea/workspace.xml
```


Authors

- Dr. Anton Chepurov (developer)
- Dr. Maksim Jenihhin from Tallinn University of Technology
- Dr. Jaan Raik from Tallinn University of Technology
