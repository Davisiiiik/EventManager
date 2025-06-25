name: Bug report
description: Report a Bug or an Issue in the plugin.
labels: "\U0001F41B Bug"
assignees: Davisiiiik


body:
  - type: markdown
    attributes:
      value: ## Plugin Manager Issues Tracker

  - id: minecraft
    type: textarea
    validations:
      required: true
    attributes:
      label: 'Minecraft Version'
      description: |
        The minecraft version your server run on. Paste an output from `/version`  command.
      placeholder: |
        [Insert Minecraft Server version]

  - id: plugin
    type: textarea
    validations:
      required: true
    attributes:
      label: 'Plugin Version'
      description: |
        The version of the plugin. Paste an output from `/version EventManager` command
      placeholder: |
        [Insert plugin version]

  - id: description
    type: textarea
    validations:
      required: true
    attributes:
      label: 'Describe the bug'
      description: |
        A clear and concise description of what the bug is.
      placeholder: |
        [Insert what exactly the problem is]

  - id: reproduction
    type: textarea
    validations:
      required: true
    attributes:
      label: 'To Reproduce'
      description: |
        Steps to reproduce the bug.
      placeholder: |
        [Insert what exactly happened and the steps to reproduce the issue]

  - id: extra
    type: textarea
    attributes:
      label: 'Additional Information'
      description: |
        Additional information that can help understanding the issue.
        Config files, clips, screenshots, etc are more than welcome.
      placeholder: |
        [Drag and drop an image or video onto this field to upload it. Otherwise please use pastebin for configs]

  - type: markdown
    attributes:
      value: |
        ## Thank you for helping improve Plugin Manager!
        Please remember to check back for any questions being asked and reply back as soon as you can!
