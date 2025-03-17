package net.pterodactylus.frimgur.insert

/**
 * Service that manages options for inserts.
 */
interface InsertOptionService {

	/**
	 * The default insert destination. Can be changed to insert to a
	 * content-based address (`"CHK@"`), or a random key (`"SSK@"`).
	 * The default is `"CHK@"`.
	 */
	var defaultInsertDestination: String

}

open class DefaultInsertOptionService : InsertOptionService {

	private var _defaultInsertDestination: String = "CHK@"
	override var defaultInsertDestination: String
		get() = _defaultInsertDestination
		set(value) {
			_defaultInsertDestination = value
		}

}
