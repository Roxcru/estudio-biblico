#!/bin/bash
set -e

mongosh "$MONGO_DATABASE" --quiet <<EOF
db.createUser({
  user: "$MONGO_APP_USER",
  pwd: "$MONGO_APP_PASSWORD",
  roles: [
    { role: "readWrite", db: "$MONGO_DATABASE" }
  ]
});

db.createCollection("usuarios");
db.createCollection("comentarios");
db.createCollection("favoritos");
db.createCollection("temas_estudio");

db.comentarios.createIndex({
  usuarioId: 1,
  "referenciaVersiculo.libro": 1,
  "referenciaVersiculo.capitulo": 1,
  "referenciaVersiculo.versiculoDesde": 1
});

db.favoritos.createIndex(
  {
    usuarioId: 1,
    "referenciaVersiculo.version": 1,
    "referenciaVersiculo.libro": 1,
    "referenciaVersiculo.capitulo": 1,
    "referenciaVersiculo.versiculoDesde": 1,
    "referenciaVersiculo.versiculoHasta": 1
  },
  { unique: true }
);

db.temas_estudio.createIndex({
  usuarioId: 1,
  fechaActualizacion: -1
});

print("Base de datos estudio_biblico inicializada correctamente");
EOF