package com.example.autoservice.scheduling;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

// Читання параметрів із JobDataMap з явною помилкою, якщо ключ відсутній (наприклад, помилка в назві)
// або значення має не той тип. Мовчки підставляти "щось" небезпечно - краще впасти з зрозумілим текстом.
final class JobParams {

    private JobParams() {
    }

    static int requireInt(JobDataMap map, String key) throws JobExecutionException {
        requireKey(map, key);
        try {
            return map.getInt(key);
        } catch (ClassCastException e) {
            throw new JobExecutionException("Параметр '" + key + "' має бути цілим числом, а не "
                    + map.get(key).getClass().getSimpleName());
        }
    }

    static String requireString(JobDataMap map, String key) throws JobExecutionException {
        requireKey(map, key);
        try {
            return map.getString(key);
        } catch (ClassCastException e) {
            throw new JobExecutionException("Параметр '" + key + "' має бути рядком, а не "
                    + map.get(key).getClass().getSimpleName());
        }
    }

    private static void requireKey(JobDataMap map, String key) throws JobExecutionException {
        if (!map.containsKey(key)) {
            throw new JobExecutionException("Параметр '" + key + "' відсутній у JobDataMap "
                    + "(перевірте назву ключа). Наявні ключі: " + String.join(", ", map.getKeys()));
        }
    }
}
