#### Task
"Can you please transfer these 600+ pages from our legacy Confluence 3.5.7 to new Confluence 7.7.3? Just keep hierarchy and text content, broken formatting can be fixed manually by someone later."

#### Problem
* Legacy Confluence export space function is not working
* Legacy Confluence page storage format differs from a new Confluence (wiki vs xhtml) 
* Content of these pages is a mess, full of unsupported macros and plugins, oddly formatted text with mixed content

#### Solution
Get pages over xml based api from legacy system, push it to new one using its json based api.




