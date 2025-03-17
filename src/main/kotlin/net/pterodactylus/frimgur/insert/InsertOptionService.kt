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

	/**
	 * The default image format. Possible values are `"png"` and `"jpeg"`.
	 */
	var defaultImageFormat: String

}

open class DefaultInsertOptionService : InsertOptionService {

	private var _defaultInsertDestination: String = "CHK@"
	override var defaultInsertDestination: String
		get() = _defaultInsertDestination
		set(value) {
			_defaultInsertDestination = value
		}

	private var _defaultImageFormat: String = "png"
	override var defaultImageFormat: String
		get() = _defaultImageFormat
		set(value) {
			_defaultImageFormat = value
		}

}
