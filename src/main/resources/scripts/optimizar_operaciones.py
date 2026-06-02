"""
Script de Innovación de Operaciones - Urban Flair
Variables de decisión:
  x1 = Unidades de camisetas a vender/mes
  x2 = Unidades de pantalones a vender/mes

Función objetivo (Maximizar ganancia):
  Z = 25000*x1 + 40000*x2

Restricciones:
  R1: x1 + x2 <= C  (capacidad total mensual)
  R2: x2 <= M       (máximo pantalones por política comercial)
  R3: x1 >= 0, x2 >= 0
"""

import sys
import json

# ───── Función objetivo ─────
PRECIO_X1 = 25000   # ganancia por camiseta
PRECIO_X2 = 40000   # ganancia por pantalón

def Z(x1, x2):
    return PRECIO_X1 * x1 + PRECIO_X2 * x2

def encontrar_optimo(capacidad, max_x2):
    """Evalúa Z en todos los vértices de la región factible y devuelve el óptimo."""
    x_intersec = max(0.0, capacidad - max_x2)

    if max_x2 <= capacidad:
        vertices = [
            (0.0, 0.0),
            (capacidad, 0.0),
            (x_intersec, max_x2),
            (0.0, max_x2)
        ]
    else:
        vertices = [
            (0.0, 0.0),
            (capacidad, 0.0),
            (0.0, capacidad)
        ]

    optimo = max(vertices, key=lambda v: Z(*v))
    return optimo, Z(*optimo)

# ───── Entry point ─────
if __name__ == '__main__':
    x1_val    = float(sys.argv[1]) if len(sys.argv) > 1 else 200.0
    x2_val    = float(sys.argv[2]) if len(sys.argv) > 2 else 100.0
    capacidad = float(sys.argv[3]) if len(sys.argv) > 3 else 500.0
    max_x2    = float(sys.argv[4]) if len(sys.argv) > 4 else 300.0

    optimo, z_opt = encontrar_optimo(capacidad, max_x2)
    z_usr = Z(x1_val, x2_val)

    print(json.dumps({
        "x1_optimo":        round(optimo[0], 2),
        "x2_optimo":        round(optimo[1], 2),
        "ganancia_optima":  round(z_opt, 2),
        "ganancia_usuario": round(z_usr, 2),
        "diferencia":       round(z_opt - z_usr, 2)
    }))