# chrome pilot

[chrome][chrome] is your airship. your helm is the [command line][conscript].

Install 

    ... 

## usage

Start chrome with a remote-debugging-port for a given port

     /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --remote-debugging-port=9222
     
Start the pilot proxy server

     chromep start
     
Issue `chromep` some commands:

Get a summary of the current tabs

     chromep tldr

Reload the current tabs

     chromep page -r
     
Open a new url

     chromep page -u='http://github.com'
     
Enable network debugging

     chromep net -e

Disable network debugging

     chromep net -d
     
Clear your browser cookies

     chromep net --clearcookies
     
Clear your browsers cache

     chromep net --clearcache
     
You can also get the docs (this readme) for chromep from the command line with

     chromep docs
     
If you find a bug you can also blame me with

     chromep issues
     
read more about the chrome remote [api][api].
     
Doug Tangren (softprops) 2012

[chrome]: https://www.google.com/intl/en/chrome/browser/
[conscript]: https://github.com/n8han/conscript
[api]:  https://developers.google.com/chrome-developer-tools/docs/protocol/1.0/index
