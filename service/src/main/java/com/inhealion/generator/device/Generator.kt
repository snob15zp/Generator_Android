package com.inhealion.generator.device

import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayInputStream
import java.io.Closeable

interface Generator: Closeable {
    /**
     *  Флаг готовности устройства - устанавливается когда от устройства получено версия и серийный номер
     **/
    val ready: Boolean

    /**
     *  Версия устройства
     **/
    val version: String?

    /**
     *  Серийный номер
     **/
    val serial: ByteArray?

    /**
     *  Пробует про-инициализироваьт устройство
     *  <returns></returns>
     **/
    fun tryToInit(): Boolean

    /**
     *  Удаляет все файлы с устройства
     *  <returns>True - если нет ошибки</returns>
     **/
    fun eraseAll(): ErrorCodes

    /**
     *  Удаляет файлы по расширению с устройства
     *  <param name="Ext">Расширение файлов для удаления</param>
     **/
    fun eraseByExt(ext: String): ErrorCodes

    /**
     *  Записывает в устройство файл
     *  <param name="FileName">Название файла</param>
     *  <param name="content">Содержимое</param>
     *  <returns>True если операция успешна</returns>
     **/
    fun putFile(fileName: String, content: ByteArrayInputStream): ErrorCodes

    /**
     *  Выполняется по записи части файла, для обновления отображения процесса
     *  Части tuple - Название файла - текущий размер, всего размер
     **/
    val fileImportProgress: Flow<Int>
}
