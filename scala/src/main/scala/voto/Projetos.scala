package voto

import scala.reflect.BeanInfo
import scala.io.Source
import scala.collection.mutable.ListBuffer
import sjson.json.Serializer.{ SJSON => serializer }
import java.io.PrintWriter
import java.io.File

@BeanInfo
case class Projeto(tipo : String, numero : Int, ano : String, data : String, ementa : String, palavras : List[String], autores : List[String])

object Projetos {

  def main(args : Array[String]) {
    val vereadores : Set[String] = Source.fromInputStream(this.getClass.getResourceAsStream("/vereador-v2.txt")).getLines.toList.
      map(line => {
        val sLine = line.split("#")
        if (sLine.size < 3)
          List()
        else
          List(sLine(1)) ++ sLine(2).split("%") ++ sLine(3).split("%")
      }).flatten.map(_.toLowerCase).toSet

    val projetos : List[Projeto] = Source.fromInputStream(this.getClass.getResourceAsStream("/projects_database.txt")).getLines.
      toList.tail.filter(line => line.startsWith("PL") || line.startsWith("PDL") || line.startsWith("PLO")).
      map(line => {
        val sLine : Array[String] = parse(line)
        val numero = sLine(1).toInt
        val temp = sLine(2).split("/")
        val ano = if (temp.size > 2) temp(2) else ""
        val palavras = if (sLine.size > 5) sLine(5).split("%").toList else List()
        val autores : List[String] = sLine(3).split("%").toList.filter(ver => vereadores.contains(ver.toLowerCase))
        Projeto(sLine(0), numero, ano, sLine(2), sLine(4), palavras, autores)
      }).filter(_.ano > "2008")
    val writer = new PrintWriter(new File("projetos.json"))

    writer.print(new String(serializer.out(projetos)))
    writer.flush
    writer.close

  }

  private def parse(line : String) : Array[java.lang.String] = {
    val buffer = ListBuffer[String]()
    var str = "";
    line.toCharArray.foreach(c => {
      c match {
        case '#' => { buffer += str; str = "" }
        case char => str += char
      }
    })
    buffer.toArray
  }

}
