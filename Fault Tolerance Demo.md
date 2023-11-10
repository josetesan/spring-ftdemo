# Fault Tolerance Demo

Esta prueba consta de 3 servicios :

- User , que simula las peticiones que vendrian de nuestros usuarios
- Portfolio, que seria nuestro propio microservicio
- StockPrice, que seria el servicio third-party al que nos conectamos.

Este servicio StockPrice está tuneado para la demo para fallar cuando recibe cierto numero de peticiones, y para devolver errores de vez en cuando, con el fin de obtener resultados temprano en la demo.

## Guion

1. Todos bien
2. StockService parado
3. Stockservice funciona, pero le empezamos a saturar

## Start

1. Arrancar servicios y ver peticiones en todos
2. Arrancar prometheus y grafana
3. Ir a grafana y ver metricas

## RunBook

1. **Happy path**

   Ver como peticiones de salida se corresponden con peticiones de entrada.

2. **Se para el third-party ( stockservice )**

   Users dan errores, errores, errores ... y siguen llamando a portfolio y portfolio a thirdparty, lo saturan.

   2.1. **Añadimos @Retry**

   Vemos que , estando el servicio parado, se incrementan el numero de conexiones.
   Se multiplican por numero de reintentos -> Sobrecarga de red.

   2.2. **Asociamos fallbackmethod**

   > ( # Limpiar la caché antes )

   En algunos casos, dependiendo de los requisitos funcionales, podremos devolver al cliente una respuesta por defecto.
   Devolvamos un precio de null.( el front cambiará este null por algo con sentido para el usuario )

   2.3 **Arrancamos stockservice**

   Con el fin de rellenar la caché con el ultimo valor conocido, lo arrancamos.
   Vemos que el usuario coge el valor de la caché ... puede ser el ultimo saldo conocido, el ultimo valor de la accion conocido .. idealmente, valores que no fluctuen mucho, o al menos no durante el tiempo que el third-party esté caido.

3. **Empezamos a sobrecargar el sistema**

   3.1. **Tiempos excesivos de carga**

   Vemos que aunque el sistema no parece fallar, a veces tarda demasiado en responder, y eso puede ser bloqueante. Algo importante para un sistema es el rendimiento, y el tiempo de espera de un cliente a una peticion. Por eso puede ser interesante introducir timeouts.

   3.2 **Introducimos timeout**

   Desafortunadamente, el servicio de timeout SOLO funciona con Futuros, CompletionStage
