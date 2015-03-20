Algorithm introduced in this section (TOPOSORT), is a key to define liner order over set of all possible RDF graphs.

## Comparing of two graphs ##
Imagine we have two graphs and want to decide which one is greater (of cause if graphs are isomorphic, they should be equal). If we could compare nodes to each other, then it would be possible to sort triples of each graph (lexicographical comparing triples to each over), and then lexicographical compare sorted lists of triples. Unfortunately we cannot compare nodes, because it is impossible to compare anonymous nodes to each other.

TOPOSORT algorithm offers a way to sort triples of graph, in such way, that lexicographical comparison becomes possible.

## Lexicographical comparison of triples and list of triples ##
Lexicographic comparison will be used quite often in this chapter, so I want to define it before head. Each triple consists of 3 nodes - _subject_, _predicate_, _object_, so triple can be treated as sequence of nodes: _s_, _p_, _o_. Also sequence of triples can be treated as sequence of nodes: _s<sub>1</sub>_, _p<sub>1</sub>_, _o<sub>1</sub>_, _s<sub>2</sub>_, _p<sub>2</sub>_, _o<sub>2</sub>_, … , _s<sub>n</sub>_, _p<sub>n</sub>_, _o<sub>n</sub>_. To lexicographically compare sequence of nodes, some lexical form can be use for URI and literal. b-nodes are special case, there is no generic way to compare them. Each variant of _comparison procedure_ introduces some way of renaming/renumbering of b-nodes, to compare them.

### Lexicographical comparison of triple lists ###
If we have to compare two list (sequences) of triples to each other, we can use following renaming technique to compare b-nodes.

For each b-node _n_ in list we can calculate _i_ - lowest index of triple in the list, which has _n_ either as _subject_ or _object_. So _sort id_ for _n_ will be _i_.`s` (if _n_ is a subject of triple _i_) of _i_.`o` (otherwise). _Sort id_ will be used to compare b-node is usage lexicographic comparison procedure.

## Property of TOPOSORT algorithm ##
TOPOSORT algorithm is used to sort triples of RDF graph (it converts set of triple into a list (sequence) of triples). Algorithm has following properties:
  1. if _G<sub>1</sub>_ and _G<sub>2</sub>_ are isomorphic, then _TOPOSORT(G<sub>1</sub>)_ = _TOPOSORT(G<sub>2</sub>)_ (using lexicographical comparison described above)
  1. if _TOPOSORT(G<sub>1</sub>)_ = _TOPOSORT(G<sub>2</sub>)_, then _G<sub>1</sub>_ and _G,,2_ are isomorphic

## Algorithm (TOPOSORT) ##
**Input:**
  * _G_ - graph
**Output:**
  * _L_ - list of triples

**Variables:**
  * Q_- set of triples
  * M_ - set of triples
  * R_- list of b-nodes (numbering table)
  * T_ - triple

#### Main algorithm ####
  1. Put all triples from _G_ to _Q_
  1. 
    * if _Q_ is empty algorithm is finished
    * if _Q_ is not empty, then calculating _M_ such, what every triple in _M_ less or equal than any other triple in _Q_ (using _comparing procedure 1_, see below)
  1. 
    * if size of _M_ is 1, then let _T_ be a triple from _M_, remove _T_ from _Q_, add _T_ to the end of _L_, if _T_ has unnumbered b-nodes, then put them into _R_ (first check subject, then object), clear _M_ and go to step 2
    * if size of _M_ greater than 1, then proceed with step 4
  1. choose triple _T_ such, that _T_ is lower or equal than any other triple in _M_ (using _comparing procedure 2_, see below), remove _T_ from _Q_, add _T_ to the end of _L_, if _T_ has unnumbered b-nodes, then put them into _R_ (first check subject, then object), clear _M_ and go to step 2

#### Comparing procedure 1 (used in algorithm) ####
Procedure is to compare two triples, using lexicographical comparison approach described above. Non-anonymous nodes are compared in usual way. To compare b-nodes list _R_ is used, if b-node is in list, then index in list used as _sort id_.  b-nodes not in table are treated as positive infinity, they are greater, than any URI, literal or numbered b-node (b-node from list _R_), but order between two such nodes is undefined.

#### Comparison procedure 2 (used in algorithm) ####
Unlike _comparison procedure 1_, this procedure uses context information (triple’s interconnections in graph), to determine their relative order. For each triple being compared, a subgraph - _derive context_ is calculated.

##### Calculating _derived context D_ for triple _T_ #####
  1. _D_ (set of triples) is empty, put _T_ to _D_
  1. for each triple _X_ in _Q_, if _X_ is having any unnumbered b-node _n_ , and if also _D_ contains triple having _n_ (either as subject or object), put _X_ to _D_
  1. if size of _D_ did not change (no new triples was added), terminate algorithm, else continue with step 2

After we have _derived context_ for both of triples, we should sort them.

##### Sorting _derived context_ #####
Input:
  * _T_ - triple
  * _D_ - set of triples (_derived context_ of _T_)
Output:
  * _C_ - list of triples

To sort we are using recursive call to _main algorithm_ (quoted variables belongs to nested context of execution)
  1. put all triples from _D_ to _Q’_
  1. put _T_ to _M’_
  1. executed _main algorithm_ from step 3
  1. assign _L’_ to _C_

Then, we have _sorted derived context_ for both triples being compared; we can determine their relative order by lexicographically comparing their contexts.

## Prove of algorithm ##
I do not have formal prove, that described algorithm has required properties, but I think it is possible to prove. My main points are:
  * Termination
    * Each cycle of _main algorithm_, reduce _Q_ by 1 triple
    * Then _main algorithm_ is used cursively, it is always reduce _Q_ at least by 1 triple, before do another recursive call (infinite recursion is impossible)
  * Property of algorithm
    * Each step (except 4) is deterministic and does not depend on any factors, except lexical presentation of non-anonymous nodes (URI and literals) and topology of graph.
    * At step 4 it will be possible, what _M_ contains to triples with equals _derived contexts_, this means, that contexts are isomorphic (and whole graph is self isomorphic) and whichever triple will be chosen at this step does not change result of sorting.