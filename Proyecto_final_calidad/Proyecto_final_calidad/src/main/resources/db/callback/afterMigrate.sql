-- afterMigrate.sql
-- Se ejecuta después de que todas las migraciones se hayan aplicado exitosamente

-- 1. Verificar que las tablas principales se crearon correctamente
SELECT 'VERIFICACION DE TABLAS PRINCIPALES' as verificacion;
SELECT
    table_name as tabla,
    table_type as tipo
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('producto', 'stock', 'users')
ORDER BY table_name;

-- 2. Verificar que las tablas de auditoría se crearon correctamente
SELECT 'VERIFICACION DE TABLAS DE AUDITORIA' as verificacion;
SELECT
    table_name as tabla_auditoria,
    table_type as tipo
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name LIKE 'au_%'
ORDER BY table_name;

-- 3. Verificar columnas de la tabla producto
SELECT 'ESTRUCTURA DE TABLA PRODUCTO' as verificacion;
SELECT
    column_name as columna,
    data_type as tipo_dato,
    is_nullable as permite_null,
    column_default as valor_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'producto'
ORDER BY ordinal_position;

-- 4. Verificar columnas de la tabla stock
SELECT 'ESTRUCTURA DE TABLA STOCK' as verificacion;
SELECT
    column_name as columna,
    data_type as tipo_dato,
    is_nullable as permite_null,
    column_default as valor_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'stock'
ORDER BY ordinal_position;

-- 5. Verificar columnas de la tabla users
SELECT 'ESTRUCTURA DE TABLA USERS' as verificacion;
SELECT
    column_name as columna,
    data_type as tipo_dato,
    is_nullable as permite_null,
    column_default as valor_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'users'
ORDER BY ordinal_position;

-- 6. Verificar constraints y relaciones
SELECT 'VERIFICACION DE FOREIGN KEYS' as verificacion;
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
         JOIN information_schema.key_column_usage AS kcu
              ON tc.constraint_name = kcu.constraint_name
                  AND tc.table_schema = kcu.table_schema
         JOIN information_schema.constraint_column_usage AS ccu
              ON ccu.constraint_name = tc.constraint_name
                  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema='public';

-- 7. Conteo de registros en cada tabla
SELECT 'CONTEO DE REGISTROS' as verificacion;
SELECT 'usuarios' as tabla, COUNT(*) as total_registros FROM users
UNION ALL
SELECT 'productos' as tabla, COUNT(*) as total_registros FROM producto
UNION ALL
SELECT 'movimientos_stock' as tabla, COUNT(*) as total_registros FROM stock;

-- 8. Verificar índices creados
SELECT 'INDICES DISPONIBLES' as verificacion;
SELECT
    tablename as tabla,
    indexname as nombre_indice
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN ('producto', 'stock', 'users')
ORDER BY tablename, indexname;

-- 9. Contenido de la tabla users (si tiene datos)
SELECT 'CONTENIDO DE TABLA USERS' as verificacion;
SELECT
    id,
    username,
    email,
    role,
    enabled
FROM users
ORDER BY id;

-- 10. Contenido de la tabla producto (si tiene datos)
SELECT 'CONTENIDO DE TABLA PRODUCTO' as verificacion;
SELECT
    id,
    nombre,
    descripcion,
    categoria,
    precio,
    cantidad,
    stock_minimo,
    esta_activo
FROM producto
ORDER BY id;

-- 11. Contenido de la tabla stock (si tiene datos)
SELECT 'CONTENIDO DE TABLA STOCK' as verificacion;
SELECT
    id,
    producto_id,
    cantidad,
    tipo,
    fecha,
    usuario
FROM stock
ORDER BY id;

-- 12. Resumen final de migración
SELECT 'RESUMEN FINAL DE MIGRACION' as verificacion;
SELECT
    NOW() as fecha_migracion,
    current_database() as base_datos,
    'MIGRACION COMPLETADA EXITOSAMENTE' as estado;