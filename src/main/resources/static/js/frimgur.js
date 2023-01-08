let formPassword

const addClipboardListener = () => {
  document.addEventListener('paste', event => {
      for (const file of event.clipboardData.files) {
        if (file.type.startsWith('image/')) {
          file.arrayBuffer().then(arrayBuffer => {
            const imageBlob = new Blob([ arrayBuffer ], { type: file.type })
            let temporaryImageId = crypto.randomUUID()
            showPlaceholder(temporaryImageId, imageBlob)
            sendImageDataToServer(imageBlob)
              .then(response => {
                const realImageId = response.headers.get('Location')
                replacePlaceholderId(temporaryImageId, realImageId)
              })
          })
        }
      }
    }
  )
}

const replacePlaceholderId = (oldId, newId) => {
  const element = document.getElementById(createImageElementId(oldId))
  element.setAttribute('id', createImageElementId(newId))
}

const showPlaceholder = (imageId, imageBlob) => {
  const placeholderElement = createPlaceholderElement(imageId)
  const canvasElement = placeholderElement.querySelector('canvas')
  drawImageToCanvas(imageBlob, canvasElement)
  document.getElementById('inserted-images').appendChild(placeholderElement)
}

const drawImageToCanvas = (imageBlob, canvasElement) => {
  const canvas = canvasElement.getContext('2d')
  decodeImage(imageBlob).then(image => {
    const pixelRatio = window.devicePixelRatio
    const canvasWidth = 300
    const canvasHeight = 150
    canvasElement.width = Math.floor(canvasWidth * pixelRatio)
    canvasElement.height = Math.floor(canvasHeight * pixelRatio)
    canvasElement.style.width = `${canvasWidth}px`
    canvasElement.style.height = `${canvasHeight}px`
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

const createPlaceholderElement = (imageId) => {
  const placeholderElement = document.createElement('div')
  placeholderElement.id = createImageElementId(imageId)
  placeholderElement.setAttribute('class', 'image-placeholder')
  const canvasElement = document.createElement('canvas')
  placeholderElement.appendChild(canvasElement)
  return placeholderElement
}

const sendImageDataToServer = (imageData, imageType) => {
  const formData = new FormData()
  formData.append('formPassword', formPassword)
  formData.append('image-type', imageType)
  formData.append('image-data', new Blob([ imageData ], { type: imageType }))
  return fetch('image/', { method: 'POST', body: formData })
}

window.onload = () => {
  formPassword = document.getElementById('form-password').textContent
  addClipboardListener()
}
