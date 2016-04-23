<h1>Index4J</h1>
Useful to search through any uploaded Javadoc.<br>
Search can be made by class name, by method name, by package name or simply by text content<br>
<br>
<h2>Todo</h2>
<ul>
<li>Searches by method return type</li>
<li>Mavenize the project so that properties can be in maven profiles
</ul>
<br>
<br>
<h2>Install notes</h2>
The static decompressed javadoc html files are going to be served from the directory name configured by setting the realLibraryPath variable. You can decide to serve the static content from outside of the tomcat context, for instance, from iis or apache, as long as tomcat has write access to that folder.