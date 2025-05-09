package hummel.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import hummel.Shop
import hummel.special.Improvable
import hummel.special.Transport
import hummel.transport.CarBasic
import hummel.transport.CarLadaImproved
import hummel.transport.CarVolkswagenImproved
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

object JsonUtils {
	class Serializer : JsonSerializer<CarBasic> {
		override fun serialize(item: CarBasic, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
			val jsonObject = JsonObject()
			jsonObject.addProperty("price", item.price)
			jsonObject.addProperty("color", item.color)
			jsonObject.addProperty("className", item.javaClass.name)
			if (item is Improvable) {
				jsonObject.addProperty("improvement", item.getImprovement())
			}
			return jsonObject
		}
	}

	fun serialize() {
		val gson =
			GsonBuilder().registerTypeHierarchyAdapter(CarBasic::class.java, Serializer()).setPrettyPrinting().create()
		val type = object : TypeToken<MutableList<CarBasic>>() {}
		val file = File("memory/transports.json")
		val json = gson.toJson(Shop.transport, type.type)
		val writer = FileWriter(file)
		writer.use { it.write(json) }
		println("List was serialized.")
	}

	fun deserialize() {
		val gson =
			GsonBuilder().registerTypeHierarchyAdapter(CarBasic::class.java, Serializer()).setPrettyPrinting().create()
		Shop.transport.clear()
		try {
			val file = File("memory/transports.json")
			val reader = FileReader(file)
			reader.use {
				val json = gson.fromJson(it, JsonArray::class.java)
				for (element in json) {
					val jsonObject = element.asJsonObject
					val price = jsonObject.get("price").asInt
					val color = jsonObject.get("color").asString
					val name = jsonObject.get("className").asString
					val clazz = StandardUtils.accessClass(name, name)
					if (clazz != null) {
						val item = clazz.getConstructor(Int::class.java, String::class.java)
							.newInstance(price, color) as Transport

						if (clazz == CarVolkswagenImproved::class.java || clazz == CarLadaImproved::class.java) {
							val improvement = jsonObject.get("improvement").asString
							item as Improvable
							item.setImprovement(improvement)
						}
						Shop.transport.add(item)
					}
				}
				println("List was deserialized.")
			}
		} catch (_: Exception) {
			Shop.transport.addAll(StandardUtils.defaultList)
			println("Error! Default list is loaded.")
		}
	}
}