package com.example.benchmark;

import com.example.control.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime) // Mide el tiempo de una única ejecución
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Configura milisegundos como unidad de tiempo
@State(Scope.Thread) // Cada hilo tiene su propia instancia
@Warmup(iterations = 0) // No realiza iteraciones de calentamiento
@Measurement(iterations = 1) // Solo realiza una iteración de medición
@Fork(1) // Se ejecuta en un solo proceso independiente
public class BenchmarkRunner {

    // Método que será ejecutado por JMH
    @Benchmark
    public void benchmarkMain() {
        Main.main(new String[]{}); // Llama al método main de tu clase Main
    }

    // Punto de entrada para ejecutar el benchmark
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkRunner.class.getSimpleName()) // Incluye esta clase
                .forks(1) // Número de procesos independientes
                .build();

        new Runner(opt).run(); // Ejecuta el benchmark
    }
}
