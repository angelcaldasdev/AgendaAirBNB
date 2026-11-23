# AgendaAirBNB

Sistema de reservas estilo Airbnb para un hotel, desarrollado en Java (NetBeans).
Proyecto de práctica basado en un caso de negocio real.

## Caso de negocio

El hotel necesita una aplicación para gestionar sus reservas. Actualmente existen
problemas de doble reserva, falta de visibilidad de habitaciones disponibles y
errores al cancelar o finalizar reservas.

## Funcionalidades principales

1. Registrar huésped
2. Consultar habitaciones disponibles
3. Crear reserva
4. Cancelar o finalizar reserva

## Reglas de negocio

- El huésped debe registrar su nombre y DNI (exactamente 8 dígitos numéricos).
- Una habitación no puede tener dos reservas con fechas cruzadas.
- Cada huésped puede tener máximo una reserva activa.
- Las habitaciones pueden estar en estado Disponible o Reservada.
- La fecha de salida debe ser posterior a la fecha de entrada.
- El sistema debe validar las operaciones y mostrar mensajes de confirmación o error.

## Tecnologías

- Java
- NetBeans IDE 32
- Git y GitHub

## Estado del proyecto

En desarrollo.
