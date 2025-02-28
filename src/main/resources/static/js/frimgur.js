let formPassword

const addClipboardListener = () => {
  document.addEventListener('paste', event => {
      for (const file of event.clipboardData.files) {
        if (file.type.startsWith('image/')) {
          file.arrayBuffer().then(arrayBuffer => {
            const imageBlob = new Blob([ arrayBuffer ], { type: file.type })
            let temporaryImageId = crypto.randomUUID()
            showPlaceholder(temporaryImageId, {}, imageBlob)
            sendImageDataToServer(imageBlob)
              .then(response => {
                const realImageId = response.headers.get('Location')
                replacePlaceholderId(temporaryImageId, realImageId)
                return realImageId
              })
              .then(imageId => refreshElementsForImage(imageId))
          })
        }
      }
    }
  )
}

const updatePlaceholderElement = (imageId, imageMetadata) => {
  const placeholderElement = getOrCreatePlaceholderElement(imageId)
  if (imageMetadata.metadata != null) {
    updateImageStatusClassName(placeholderElement, imageMetadata.metadata.status)
    placeholderElement.querySelector('.change-width input').value = imageMetadata.metadata.width
    placeholderElement.querySelector('.change-height input').value = imageMetadata.metadata.height
    const statusNode = document.createTextNode(`${imageMetadata.metadata.status}`)
    placeholderElement.querySelector('.filename input').value = imageMetadata.metadata.filename
    placeholderElement.querySelector('.status').replaceChildren(statusNode)
    const keyNode = document.createTextNode(imageMetadata.metadata.key ? imageMetadata.metadata.key : '')
    let linkToKey = placeholderElement.querySelector('.key a')
    linkToKey.setAttribute('href', `/${imageMetadata.metadata.key}`)
    linkToKey.replaceChildren(keyNode)
  }
}

const removePlaceholderElement = imageId => {
  const placeholderElement = document.getElementById(createImageElementId(imageId))
  if (placeholderElement) {
    placeholderElement.remove()
  }
}

const refreshElementsForImage = (imageId) =>
  fetch(`image/${imageId}`)
      .then(response => {
        if (response.status === 404) {
          throw "Not Found"
        }
        return response
      })
      .then(response => response.json())
      .then(imageMetadata => {
        updatePlaceholderElement(imageId, imageMetadata)
        fetchImageDataFromServer(imageId).then(imageBlob => {
          showPlaceholder(imageId, imageMetadata, imageBlob)
        })
      })
      .catch(() => removePlaceholderElement(imageId))

const replacePlaceholderId = (oldId, newId) => {
  const element = document.getElementById(createImageElementId(oldId))
  element.setAttribute('id', createImageElementId(newId))
}

const updateImageStatusClassName = (element, newClassName) => {
  element.className = element.className.replaceAll(/\bimage-status-[^ ]*\b/g, "") + " " + ("image-status-" + newClassName.toLowerCase())
}

const setImageFilename = (placeholderElement, imageId) => {
  const inputField = placeholderElement.querySelector('.filename input')
  return fetch(`image/${imageId}`, { method: 'PATCH', body: JSON.stringify({ filename: inputField.value }) })
}

const showPlaceholder = (imageId, imageMetadata, imageBlob) => {
  const placeholderElement = getOrCreatePlaceholderElement(imageId)
  if (imageMetadata.metadata != null) {
    updatePlaceholderElement(imageId, imageMetadata)
  }
  const canvasElement = placeholderElement.querySelector('canvas')
  const canvasWidth = 300
  const canvasHeight = 150
  canvasElement.style.width = `${canvasWidth}px`
  canvasElement.style.height = `${canvasHeight}px`
  drawImageToCanvas(imageBlob, canvasElement, canvasWidth, canvasHeight)
}

const drawImageToCanvas = (imageBlob, canvasElement, canvasWidth, canvasHeight) => {
  const canvas = canvasElement.getContext('2d')
  decodeImage(imageBlob).then(image => {
    const pixelRatio = window.devicePixelRatio
    canvasElement.width = Math.floor(canvasWidth * pixelRatio)
    canvasElement.height = Math.floor(canvasHeight * pixelRatio)
    canvas.scale(pixelRatio, pixelRatio)
    const widthRatio = image.width / canvasWidth
    const heightRatio = image.height / canvasHeight
    const downscaleFactor = Math.max(widthRatio, heightRatio)
    canvas.drawImage(image, (canvasWidth - (image.width / downscaleFactor)) / 2, (canvasHeight - (image.height / downscaleFactor)) / 2, image.width / downscaleFactor, image.height / downscaleFactor)
  })
}

const decodeImage = (imageBlob) => {
  const image = new Image()
  image.src = URL.createObjectURL(imageBlob)
  return image.decode().then(() => Promise.resolve(image))
}

const createImageElementId = (imageId) => `image-${imageId}`

const changeDimension = (placeholderElement, imageId, inputSelector, dimension) => {
  const newValue = placeholderElement.querySelector(inputSelector).value
  return fetch(`image/${imageId}`, { method: 'PATCH', body: JSON.stringify({ [dimension]: newValue }) })
      .then(response => {
        if ((response.status >= 200) && (response.status < 300)) {
          return response.headers.get('location')
        }
      })
      .then(newImageId =>
          refreshElementsForImage(imageId)
              .then(() => newImageId)
      )
      .then(newImageId => {
        getOrCreatePlaceholderElement(newImageId)
        return refreshElementsForImage(newImageId)
      })
}

const copyKeyToClipboard = (placeholderElement) =>
  navigator.clipboard.writeText(placeholderElement.querySelector('.key a').textContent)

const getOrCreatePlaceholderElement = (imageId) => {
  const existingPlaceholderElement = document.getElementById(createImageElementId(imageId))
  if (existingPlaceholderElement != null) {
    return existingPlaceholderElement
  }
  const placeholderTemplateElement = document.getElementById('image-template')
  const placeholderElement = placeholderTemplateElement.cloneNode(true)
  placeholderElement.setAttribute('id', createImageElementId(imageId))
  const getImageIdFromElement = () => placeholderElement.getAttribute('id').replace(/^image-/, '')
  placeholderElement.querySelector('button.button-refresh').addEventListener('click', () => refreshElementsForImage(getImageIdFromElement()))
  placeholderElement.querySelector('.change-width button').addEventListener('click', () => changeDimension(placeholderElement, getImageIdFromElement(), '.change-width input', 'width'))
  placeholderElement.querySelector('.change-height button').addEventListener('click', () => changeDimension(placeholderElement, getImageIdFromElement(), '.change-height input', 'height'))
  placeholderElement.querySelector('.filename input').addEventListener('change', () => setImageFilename(placeholderElement, getImageIdFromElement()))
  placeholderElement.querySelector('.filename button').addEventListener('click', () => setImageFilename(placeholderElement, getImageIdFromElement()))
  placeholderElement.querySelector('button.button-start').addEventListener('click', () =>
      fetch(`image/${getImageIdFromElement()}`, { method: 'PATCH', body: JSON.stringify({ status: 'Inserting' }) })
          .then(() => refreshElementsForImage(getImageIdFromElement()))
  )
  placeholderElement.querySelector('button.button-remove').addEventListener('click', () =>
    fetch(`image/${getImageIdFromElement()}`, { method: 'DELETE' })
        .then(() => placeholderElement.remove())
  )
  placeholderElement.querySelector('.key button').addEventListener('click', () => copyKeyToClipboard(placeholderElement))
  document.getElementById('inserted-images').appendChild(placeholderElement)
  return placeholderElement
}

const sendImageDataToServer = (imageBlob) => {
  const formData = new FormData()
  formData.append('formPassword', formPassword)
  formData.append('image-data', imageBlob)
  return fetch('image/', { method: 'POST', body: formData })
}

const fetchImageDataFromServer = (imageId) =>
  fetch(`image-data/${imageId}`)
    .then(response => ({ data: response.arrayBuffer(), contentType: response.headers.get('Content-Type') }))
    .then(({ data, contentType }) => data.then(arrayBuffer => ({ data: arrayBuffer, contentType })))
    .then(({ data, contentType }) => new Blob([ data ], { type: contentType }))

const populateWithExistingImages = () => {
  fetch('images/')
    .then(response => response.json())
    .then(response => {
      for (const imageMetadata of response) {
        getOrCreatePlaceholderElement(imageMetadata.id)
      }
      for (const imageMetadata of response) {
        fetchImageDataFromServer(imageMetadata.id).then(imageBlob => {
          showPlaceholder(imageMetadata.id, imageMetadata, imageBlob)
        })
      }
    })
}

const checkIfOnMacOsAndChangeShortcut = () => {
  if (navigator.platform === 'MacIntel') {
    document.getElementById('on-mac').className = ''
    document.getElementById('on-everything-else').className = 'hidden'
  }
}

window.onload = () => {
  formPassword = document.getElementById('form-password').textContent
  checkIfOnMacOsAndChangeShortcut()
  populateWithExistingImages()
  addClipboardListener()
}
