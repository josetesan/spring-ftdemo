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

   > Añadimos 4 usuarios

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

   > Añadimos hasta 5 usuarios

   3.1. **Tiempos excesivos de carga**

   Vemos que aunque el sistema no parece fallar, a veces tarda demasiado en responder, y eso puede ser bloqueante. Algo importante para un sistema es el rendimiento, y el tiempo de espera de un cliente a una peticion. Por eso puede ser interesante introducir timeouts.

   3.2 **Introducimos timeout**

   Introducimos timeout, con lo que vemos que el cliente no espera suficiente tiempo, pero dependiendo del requisito funcional, puede ser lo que queramos que vea o no.
   Para ello, podemos trabajar con un fallback

   Cuando vemos que un servicio falla mucho, el patron base a usar es el circuit-breaker, con lo cual conseguimos no saturar el servicio destino y darle tiempo que se recupere, si es que tenia sobrecarga.

   3.3. **Circuit breaker**

   Vemos que en el third-party hay spikes, y luego hay vacios : es el circuit breaker haciendo su trabajo. Intenta durante un tiempo configurable , y si el numero de errores es mayor del deseado, corta el trafico, aliviando entre otras cosas el trafico de red.
   Al rato, vuelve a probar a ver si funciona, y si es asi, cierra el circuito y permite todas las conexiones.

   3.4. **BulkHead**

   Pero esto es un arma de doble filo : lo que está haciendo el _circuitbreaker_ es , que cada vez que se cierra el circuito, le machaca con todas las peticiones que tuviera en curso, lo cual puede volver a sobrecargar el sistema destino y volveriamos a empezar.
   Para eso, lo mas adecuado es usa un control de la concurrencia, que en resiliencia se implementa con el patron BulkHead, aislar al sistema de un fallo de sus partes.

   Ver que maxConcurrentCalls: 50 == Current in flight requests: 50
