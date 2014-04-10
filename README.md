#Centrality Project - Marcin Swieczkowski

This is my project for CS 591 Data Mining with Terzi and Erdos. My goal is to experimentally evaluate algorithms that solve the following problem: **Given a graph and a group of nodes within that graph, add k edges that maximize the centrality of the group.**

###Importing Graphs

Graphs can be imported from regular files with tab-delimited data.

You have to explicitly specify whether the graph is directed or not.

###Exporting Graphs

Graphs can be exported to .dot format, which is the format used by graphviz to create graphic visualizations of graphs. Use dot for directed graphs, and neato for undirected graphs.