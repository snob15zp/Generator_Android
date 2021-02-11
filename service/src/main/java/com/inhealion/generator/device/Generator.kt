package com.inhealion.generator.device

import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayInputStream

interface Generator {
    /// <summary>
    /// Флаг готовности устройства - устанавливается когда от устройства получено версия и серийный номер
    /// </summary>
    val ready: Boolean

    /// <summary>
    /// Версия устройства
    /// </summary>
    val version: String

    /// <summary>
    /// Серийный номер
    /// </summary>
    val serial: ByteArray

    /// <summary>
    /// Пробует про-инициализироваьт устройство
    /// </summary>
    /// <returns></returns>
    fun tryToInit(): Boolean

    /// <summary>
    /// Удаляет все файлы с устройства
    /// </summary>
    /// <returns>True - если нет ошибки</returns>
    fun eraseAll(): ErrorCodes

    /// <summary>
    /// Удаляет файлы по расширению с устройства
    /// </summary>
    /// <param name="Ext">Расширение файлов для удаления</param>
    /// <returns></returns>
    fun eraseByExt(ext: String): ErrorCodes

    /// <summary>
    /// Записывает в устройство файл
    /// </summary>
    /// <param name="FileName">Название файла</param>
    /// <param name="content">Содержимое</param>
    /// <returns>True если операция успешна</returns>
    fun putFile(fileName: String, content: ByteArrayInputStream): ErrorCodes

    /// <summary>
    /// Выполняется по записи части файла, для обновления отображения процесса
    /// Части tuple - Название файла - текущий размер, всего размер
    /// </summary>
    val putFilePart: Flow<Triple<String, Int, Int>>

    /// <summary>
    /// Отключение устройства
    /// </summary>
    fun disconnect();

    /// <summary>
    /// Загрузка одной страницы прошивки (chunk из xml файла)
    /// </summary>
    /// <returns></returns>
    fun bootloaderUploadMcuFwChunk(chunk: ByteArray): Boolean

    /// <summary>
    /// Запускает записанную прошивку
    /// </summary>
    fun bootloaderRunMcuFw()

    /// <summary>
    /// Перезагружает в бутлоадер
    /// </summary>
    fun bootloaderReset()
}
