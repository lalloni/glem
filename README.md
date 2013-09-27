glem
====

This tiny web application exposes an HTTP endpoint for routing GitLab event
notifications to email.

Configuration
-------------

You must setup an SMTP/S server account in application.conf.

Push notifications
------------------

Just add the following endpoint to your GitLab project web hooks:

```
http://<host>:<port>/push?recipients=<email1>,<email2>,...
```

Replacing the host/port of the box running this app and adding as much recipient
emails as needed.
