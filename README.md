##Architettura

- Ogni nodo fa partire un Server e un client
- Il client comunica unicamente con il suo server locale
- Il server gestisce lo stato del gioco, ovvero prende tutto quello che fa il suo client locale 
e lo propaga agli altri server. Inoltre rimane in ascolto per applicare le modifiche effettuate dagli altri client.


##Connessione
- In uno stato iniziale supponiamo di non avere nessun nodo attivo. Il primo nodo X parte, e fa partire il suo server
e il suo client. In questo momento può cominciare a giocare da solo. Un altro giocatore Y decide che vuol giocare con 
  lui. In questo caso il server di Y, conoscendo l'ip e porta di X, apre una connessione verso Y.
  Y comunica a X tutti i server attualmente attivi e avvia una connessione con tutti i server. Ora anche Y giocare con X.
  Chiaramente Y riceve lo stato attuale del gioco.
  
##Gestione della concorrenza
La concorrenza è gestita usando l'algoritmo di Agrawala ovvero:
- Supponiamo di avere due nodi x e y
- X e Y fanno partire rispettivamente client e server e supponiamo che si siano già connessi per giocare
- Inseriamo un terzo nodo Z che si collega a Y. Y in questo caso gli comunica che è connesso anche con X. Z dunque apre
una connessione anche con X.
- Supponiamo che ora X esegue una mossa e che Y e Z non abbiano fatto nulla. Il server di X in questo caso manda una richiesta
a Y e Z di tipo Message.REQUEST (incluso anche il suo timestamp) per richiedere il permesso di eseguire la sezione critica. Dato che Y e Z non stanno eseguendo la
sezione critica, gli rispondo con un Message.PERMIT. X dunque verifica di aver ricevuto PERMIT da tutti i due nodi. In caso
  positivo manda in broadcast la mossa che ha eseguito.

- Supponiamo ora che X abbia fatto una mossa e che quindi è nella sezione critica, ma anche Y ha fatto una mossa e anche
lui è nella sezione critica. Sviluppiamo dalla parte di X. Dato che anche X sta cercando di entrare nella sezione critica,
  alla richiesta di Y, X deve fare delle verifiche: 
  - Se hanno cliccato punti del puzzle diversi, allora X manda un messaggio di tipo Message.PERMIT
  - Se hanno cliccato almeno un puzzle uguale, allora X confronta il suo timestamp con quello di Y. Se Y ha priorità 
  maggiore allora X risponde con un Message.PERMIT. Se invece X ha priorità maggiore manda un Message.NOTPERMIT a Y.
  - Se hanno cliccato almeno un puzzle uguale e hanno timestamp uguali, allora nel dubbio X manda un PERMIT a Y.
  
- Se X invece non avesse fatto nessun mossa, risponderebbe con un Message.PERMIT  
