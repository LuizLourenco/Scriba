#!/bin/bash
# start.sh — Inicia o Scriba
# Uso: ./start.sh

# Verificar se Java está instalado
if ! command -v java &> /dev/null; then
  echo "❌ Java não encontrado. Baixe em: https://adoptium.net"
  echo "   Instale o Java 25 e execute este script novamente."
  exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | grep -o '[0-9]*' | head -1)
if [ "$JAVA_VER" -lt 21 ]; then
  echo "⚠️  Java $JAVA_VER encontrado. Recomendado: Java 25+"
fi

echo "📚 Iniciando Scriba..."
echo "   Banco de dados: ~/scriba/biblioteca.db"
echo "   Acesse no navegador: http://localhost:8080"
echo "   Para parar: Ctrl+C"
echo ""

# Criar pasta de dados se não existir
mkdir -p ~/scriba

# Iniciar a aplicação
java -Xmx256m -jar scriba.jar
