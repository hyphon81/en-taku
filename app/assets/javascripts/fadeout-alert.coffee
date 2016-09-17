$ ->
  window.setTimeout (->
    $(".alert").fadeTo(1500, 0).slideUp 500, ->
      $(this).remove()
  ), 2000

