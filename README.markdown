# Dashboard

This is a simple dashboard for a small team collaboration written in clojure using Compojure & MongoDB.
It's a first attempt to use Compojure for web develpment and it's a in very alpha state.
Lots of bugs and lacks of functionality.

## Usage

1. Install [Leiningen](https://github.com/technomancy/leiningen)
2. Install [MongoDB](http://www.mongodb.org/)
3. Install Dashboard:
        $ git clone git://github.com/jardev/dashboard.git
        $ cd dashboard
        $ lein deps
4. Check settings in dashboard/src/net/jardev/dashboard/config.clj
5. Run:
        $ lein daemon start web
6. Add a user:
        $ lein repl
        user=> (require '[net.jardev.dashboard.api.db :as db])
        user=> (db/add-user {:username "demo" :password "demo" :roles #{"user"}})
7. Got to http://127.0.0.1:8000/
8. Log in as demo/demo


## TODO
1. Notification Service for users without ETA
2. Registration
3. Users statistics
4. Tasks
5. Tasks statistics
6. Reports

## License

Copyright (C) 2010 jardev

Distributed under the Eclipse Public License, the same as Clojure.
