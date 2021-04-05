package com.inhealion.generator.device

import kotlinx.coroutines.flow.Flow
import java.io.Closeable


interface Generator : Closeable {
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
    fun putFile(fileName: String, content: ByteArray): ErrorCodes

    /**
     * Reboot device
     */
    fun reboot(): Boolean

    /**
     * Transmit session is done
     */
    fun transmitDone()

    /**
     *  Выполняется по записи части файла, для обновления отображения процесса
     *  Части tuple - Название файла - текущий размер, всего размер
     **/
    val fileImportProgress: Flow<FileImport>

    companion object {
        fun romBAPrepareMCUFirmware(chunk: ByteArray): Collection<Byte> {
            val resp = mutableListOf<Byte>()
            var iter = 0
            while (iter < chunk.size) {
                resp.add(chunk[iter])
                if (iter + 1 < chunk.size && chunk[iter] == 0xba.toByte() && chunk[iter + 1] == 0.toByte()) {
                    println("NNN > skip zero in 0xba00")
                    iter++
                }
                iter++
            }
            return resp
        }

    }
}

data class FileImport(val fileName: String, val progress: Int)
