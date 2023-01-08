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
  const canvasWidth = 300
  const canvasHeight = 150
  drawImageToCanvas(imageBlob, canvasElement, canvasWidth, canvasHeight).then((image) => {
    const dimensionsNode = document.createTextNode(`${image.width} × ${image.height}`)
    placeholderElement.querySelector('.dimensions').appendChild(dimensionsNode)
    canvasElement.style.width = `${canvasWidth}px`
    canvasElement.style.height = `${canvasHeight}px`
    placeholderElement.style.width = `${canvasWidth}px`
    placeholderElement.style.height = `${canvasHeight}px`
  })
  document.getElementById('inserted-images').appendChild(placeholderElement)
}

const drawImageToCanvas = (imageBlob, canvasElement, canvasWidth, canvasHeight) => {
  const canvas = canvasElement.getContext('2d')
  return decodeImage(imageBlob).then(image => {
    const pixelRatio = window.devicePixelRatio
    canvasElement.width = Math.floor(canvasWidth * pixelRatio)
    canvasElement.height = Math.floor(canvasHeight * pixelRatio)
    canvas.scale(pixelRatio, pixelRatio)
    const widthRatio = image.width / canvasWidth
    const heightRatio = image.height / canvasHeight
    const downscaleFactor = Math.max(widthRatio, heightRatio)
    canvas.drawImage(image, (canvasWidth - (image.width / downscaleFactor)) / 2, (canvasHeight - (image.height / downscaleFactor)) / 2, image.width / downscaleFactor, image.height / downscaleFactor)
    return Promise.resolve(image)
  })
}

const decodeImage = (imageBlob) => {
  const image = new Image()
  image.src = URL.createObjectURL(imageBlob)
  return image.decode().then(() => Promise.resolve(image))
}

const createImageElementId = (imageId) => `image-${imageId}`

const createPlaceholderElement = (imageId) => {
  const placeholderTemplateElement = document.getElementById('image-template')
  const placeholderElement = placeholderTemplateElement.cloneNode(true)
  placeholderElement.setAttribute('id', createImageElementId(imageId))
  return placeholderElement
}

const sendImageDataToServer = (imageBlob) => {
  const formData = new FormData()
  formData.append('formPassword', formPassword)
  formData.append('image-data', imageBlob)
  return fetch('image/', { method: 'POST', body: formData })
}

window.onload = () => {
  formPassword = document.getElementById('form-password').textContent
  addClipboardListener()
}
