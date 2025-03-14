let formPassword

const addClipboardListener = () => {
  document.addEventListener('paste', event => {
    for (const file of event.clipboardData.files) {
      if (file.type.startsWith('image/')) {
        file.arrayBuffer().then(arrayBuffer => {
          const imageBlob = new Blob([arrayBuffer], { type: file.type })
          let temporaryImageId = crypto.randomUUID()
          getOrCreatePlaceholderElement(temporaryImageId)
          sendImageDataToServer(imageBlob)
              .then(response => {
                const realImageId = response.headers.get('Location')
                replacePlaceholderId(temporaryImageId, realImageId)
                return realImageId
              })
              .then(imageId => {
                return refreshElementsForImage(imageId)
                    .then(() => getOrCreatePlaceholderElement(imageId).querySelector('.filename input'))
                    .then(inputElement => {
                      inputElement.focus()
                      inputElement.select()
                    })
              })
        })
      }
    }
  })
}

const updatePlaceholderElement = (imageId, imageMetadata) => {
  const placeholderElement = getOrCreatePlaceholderElement(imageId)
  if (imageMetadata.metadata != null) {
    updateImageStatusClassName(placeholderElement, imageMetadata.metadata.status)
    placeholderElement.querySelector('.change-width input').value = imageMetadata.metadata.width
    placeholderElement.querySelector('.change-height input').value = imageMetadata.metadata.height
    placeholderElement.querySelector('.filename input').value = imageMetadata.metadata.filename
    placeholderElement.querySelector('.inserting-as .filename').textContent = imageMetadata.metadata.insertFilename
    let linkToKey = placeholderElement.querySelector('.key a')
    linkToKey.setAttribute('href', `/${imageMetadata.metadata.key}`)
    linkToKey.textContent = imageMetadata.metadata.key ? imageMetadata.metadata.key : ''
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
      })
      .then(() => getOrCreatePlaceholderElement(imageId))
      .then(placeholderElement => placeholderElement.querySelector('.image-container .image').src = `image-data/${imageId}`)
      .catch(() => removePlaceholderElement(imageId))

const replacePlaceholderId = (oldId, newId) => {
  const element = document.getElementById(createImageElementId(oldId))
  element.setAttribute('id', createImageElementId(newId))
}

const updateImageStatusClassName = (element, newClassName) => {
  element.className = element.className.replaceAll(/\bimage-status-[^ ]*\b/g, "") + " " + ("image-status-" + newClassName.toLowerCase())
}

const createImageElementId = (imageId) => `image-${imageId}`

const changeDimension = (placeholderElement, imageId, inputSelector, dimension) =>
    getFilename(imageId)
        .then(filename => [filename, placeholderElement.querySelector(inputSelector).value])
        .then(([filename, size]) => fetch(`image/${imageId}`, { method: 'PATCH', body: JSON.stringify({ filename: filename, [dimension]: size }) }))
        .then(response => {
          if ((response.status >= 200) && (response.status < 300)) {
            return response.headers.get('location')
          }
        })
        .then(newImageId =>
            refreshElementsForImage(imageId)
                .then(() => newImageId)
        )
        .then(newImageId => [newImageId, getOrCreatePlaceholderElement(newImageId)])
        .then(([newImageId, placeholderElement]) =>
            refreshElementsForImage(newImageId)
                .then(() => placeholderElement)
        )
        .then(placeholderElement => placeholderElement.querySelector('.filename input'))
        .then(inputElement => {
          inputElement.focus()
          inputElement.select()
        })

const copyKeyToClipboard = (placeholderElement) =>
  navigator.clipboard.writeText(placeholderElement.querySelector('.key a').textContent)

const getFilename = imageId => Promise.resolve(getOrCreatePlaceholderElement(imageId))
    .then(placeholderElement => placeholderElement.querySelector('.filename input'))
    .then(inputField => inputField.value)

const startInsert = imageId =>
    getFilename(imageId)
        .then(filename => fetch(`image/${imageId}`, { method: 'PATCH', body: JSON.stringify({ filename: filename, status: 'Inserting' }) }))
        .then(() => refreshElementsForImage(imageId))

const getOrCreatePlaceholderElement = (imageId) => {
  const existingPlaceholderElement = document.getElementById(createImageElementId(imageId))
  if (existingPlaceholderElement != null) {
    return existingPlaceholderElement
  }
  const placeholderTemplateElement = document.getElementById('image-template')
  const placeholderElement = placeholderTemplateElement.cloneNode(true)
  placeholderElement.setAttribute('id', createImageElementId(imageId))
  const getImageIdFromElement = () => placeholderElement.getAttribute('id').replace(/^image-/, '')
  placeholderElement.querySelector('.image-container .image').src = `image-data/${imageId}`
  placeholderElement.querySelector('button.button-refresh').addEventListener('click', () => refreshElementsForImage(getImageIdFromElement()))
  placeholderElement.querySelector('.change-width button').addEventListener('click', () => changeDimension(placeholderElement, getImageIdFromElement(), '.change-width input', 'width'))
  placeholderElement.querySelector('.change-height button').addEventListener('click', () => changeDimension(placeholderElement, getImageIdFromElement(), '.change-height input', 'height'))
  placeholderElement.querySelector('.filename input').addEventListener('keypress', keyDownEvent => {
      if (keyDownEvent.code === 'Enter') {
          return startInsert(getImageIdFromElement())
      }
  })
  placeholderElement.querySelector('button.button-start').addEventListener('click', () => startInsert(getImageIdFromElement()))
  placeholderElement.querySelector('button.button-remove').addEventListener('click', () =>
    fetch(`image/${getImageIdFromElement()}`, { method: 'DELETE' })
        .then(() => placeholderElement.remove())
  )
  placeholderElement.querySelector('.key button').addEventListener('click', () => copyKeyToClipboard(placeholderElement))
  document.getElementById('inserted-images').prepend(placeholderElement)
  return placeholderElement
}

const sendImageDataToServer = (imageBlob) => {
  const formData = new FormData()
  formData.append('formPassword', formPassword)
  formData.append('image-data', imageBlob)
  return fetch('image/', { method: 'POST', body: formData })
}

const populateWithExistingImages = () => {
  fetch('images/')
    .then(response => response.json())
    .then(response => {
      for (const imageMetadata of response) {
        updatePlaceholderElement(imageMetadata.id, imageMetadata)
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
  if (!window.isSecureContext) {
    document.querySelector('.not-secure-context').classList.remove('hidden')
  }
  formPassword = document.getElementById('form-password').textContent
  checkIfOnMacOsAndChangeShortcut()
  populateWithExistingImages()
  addClipboardListener()
}
