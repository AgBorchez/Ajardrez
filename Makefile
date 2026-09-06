.PHONY: all build engine gui run clean

# Meta por defecto: compila todo
all: build

# Compila el motor (C++) y empaqueta la GUI (Java)
build: engine gui

engine:
	@echo "==> Compilando motor en C++..."
	cmake -S engine -B engine/build
	cmake --build engine/build

gui:
	@echo "==> Compilando interfaz en Java..."
	mvn -f gui/pom.xml clean package -DskipTests

# Ejecuta la interfaz gráfica (asegurando compilación previa)
run: build
	@echo "==> Iniciando Ajedrez..."
	java -jar gui/target/gui-1.0-SNAPSHOT.jar

# Limpia los binarios y carpetas generadas
clean:
	@echo "==> Limpiando compilaciones..."
	rm -rf engine/build
	mvn -f gui/pom.xml clean